package site.holliverse.infra.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import site.holliverse.customer.application.usecase.recommendation.RecommendationPendingFutureRegistry;
import site.holliverse.customer.application.usecase.recommendation.RecommendationResult;
import site.holliverse.customer.integration.kafka.dto.RecommendationMessagePayload;
import site.holliverse.customer.persistence.entity.PersonaRecommendation;
import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.customer.persistence.repository.PersonaRecommendationRepository;
import site.holliverse.shared.monitoring.CustomerMetrics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class RecommendationKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final PersonaRecommendationRepository personaRecommendationRepository;
    private final RecommendationPendingFutureRegistry pendingFutureRegistry;
    private final MeterRegistry meterRegistry;
    private final CustomerMetrics customerMetrics;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();

    @KafkaListener(
            topics = "${spring.kafka.topic.recommendation}",
            groupId = "${app.kafka.recommendation-consumer-group-id}",
            containerFactory = "recommendationKafkaListenerContainerFactory"
    )
    public void consume(
            String payload,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        Long memberIdForCleanup = null;
        Timer.Sample sample = Timer.start(meterRegistry);
        var customerSample = customerMetrics.startSample();
        String outcome = "error";
        try {
            log.debug("[Kafka][recommendation] received. topic={}, offset={}, raw={}", topic, offset, payload);

            RecommendationMessagePayload message = objectMapper.readValue(
                    payload, RecommendationMessagePayload.class);

            memberIdForCleanup = message.memberId();

            List<RecommendedProductItem> products = message.recommendedProducts() == null
                    ? Collections.emptyList()
                    : message.recommendedProducts().stream()
                    .map(p -> new RecommendedProductItem(
                            p.rank(),
                            p.productId(),
                            p.productName(),
                            p.productType(),
                            p.productPrice(),
                            p.salePrice(),
                            p.tags() != null ? p.tags() : Collections.emptyList(),
                            p.reason()
                    ))
                    .toList();

            String cachedText = message.cachedLlmRecommendation() != null ? message.cachedLlmRecommendation() : "";

            log.info(
                    "[Kafka][recommendation] upsert. memberId={}, segment={}, productCount={}",
                    message.memberId(), message.segment(), products.size()
            );

            PersonaRecommendation saved = personaRecommendationRepository
                    .findById(message.memberId())
                    .map(entity -> {
                        entity.updateRecommendation(message.segment(), cachedText, products);
                        return personaRecommendationRepository.save(entity);
                    })
                    .orElseGet(() -> personaRecommendationRepository.save(
                            PersonaRecommendation.builder()
                                    .memberId(message.memberId())
                                    .segment(message.segment())
                                    .cachedLlmRecommendation(cachedText)
                                    .recommendedProducts(products)
                                    .build()));

            CompletableFuture<RecommendationResult> future = pendingFutureRegistry.remove(message.memberId());
            if (future != null) {
                future.complete(RecommendationResult.fromEntity(saved, RecommendationResult.RecommendationSource.FASTAPI));
                outcome = "completed_pending";
                log.debug(
                        "[Kafka][recommendation] future completed. memberId={}, updatedAt={}",
                        message.memberId(), saved.getUpdatedAt()
                );
            } else {
                outcome = "stored_without_waiter";
                log.debug(
                        "[Kafka][recommendation] no pending future. memberId={}, updatedAt={}",
                        message.memberId(), saved.getUpdatedAt()
                );
            }

            ack.acknowledge();
            log.debug("[Kafka][recommendation] acked. topic={}, offset={}", topic, offset);
            counter("success").increment();
            sample.stop(timer("success"));
        } catch (Exception e) {
            if (memberIdForCleanup != null) {
                CompletableFuture<RecommendationResult> future = pendingFutureRegistry.remove(memberIdForCleanup);
                if (future != null) {
                    future.completeExceptionally(e);
                }
            }
            log.error("[Kafka][recommendation] consume failed. topic={}, offset={}, raw={}", topic, offset, payload, e);
            errorCounter(e.getClass().getSimpleName()).increment();
            counter("error").increment();
            sample.stop(timer("error"));
            throw new IllegalStateException("recommendation consume failed", e);
        } finally {
            customerMetrics.stopRecommendationKafkaConsume(customerSample, outcome);
        }
    }

    private Counter counter(String outcome) {
        return counters.computeIfAbsent(outcome, ignored ->
                Counter.builder("holliverse.kafka.consume")
                        .description("Kafka consume result count")
                        .tag("consumer", "recommendation")
                        .tag("outcome", outcome)
                        .register(meterRegistry));
    }

    private Timer timer(String outcome) {
        return timers.computeIfAbsent(outcome, ignored ->
                Timer.builder("holliverse.kafka.consume.duration")
                        .description("Kafka consumer processing duration")
                        .tag("consumer", "recommendation")
                        .tag("outcome", outcome)
                        .register(meterRegistry));
    }

    private Counter errorCounter(String exception) {
        return errorCounters.computeIfAbsent(exception, ignored ->
                Counter.builder("holliverse.kafka.consume.errors")
                        .description("Recommendation consumer failures by exception")
                        .tag("consumer", "recommendation")
                        .tag("exception", exception)
                        .register(meterRegistry));
    }
}
