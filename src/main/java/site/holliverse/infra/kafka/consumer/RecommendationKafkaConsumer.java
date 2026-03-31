package site.holliverse.infra.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.concurrent.CompletableFuture;

/**
 * recommendation-topic 메시지 수신 → persona_recommendation upsert → 대기 중인 CompletableFuture 완료.
 */
@Slf4j
@RequiredArgsConstructor
public class RecommendationKafkaConsumer {
    private static final String TRACE_ID_FALLBACK = "NA";

    private final ObjectMapper objectMapper;
    private final PersonaRecommendationRepository personaRecommendationRepository;
    private final RecommendationPendingFutureRegistry pendingFutureRegistry;
    private final CustomerMetrics customerMetrics;

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
        long consumeStartedAt = System.nanoTime();
        Long memberIdForCleanup = null;
        String traceId = TRACE_ID_FALLBACK;
        var timerSample = customerMetrics.startSample();
        String outcome = "error";
        try {
            log.info(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} topic={} offset={}",
                    traceId, "unknown", "received", elapsedMs(consumeStartedAt), topic, offset
            );

            RecommendationMessagePayload message = objectMapper.readValue(
                    payload, RecommendationMessagePayload.class);

            memberIdForCleanup = message.memberId();
            traceId = message.traceId() != null && !message.traceId().isBlank()
                    ? message.traceId()
                    : TRACE_ID_FALLBACK;
            log.info(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} topic={} offset={}",
                    traceId, message.memberId(), "parsed", elapsedMs(consumeStartedAt), topic, offset
            );

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
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} segment={} product_count={}",
                    traceId, message.memberId(), "upsert_start", elapsedMs(consumeStartedAt),
                    message.segment(), products.size()
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
            log.info(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} updated_at={}",
                    traceId, message.memberId(), "upsert_done", elapsedMs(consumeStartedAt), saved.getUpdatedAt()
            );

            CompletableFuture<RecommendationResult> future = pendingFutureRegistry.remove(message.memberId());
            if (future != null) {
                future.complete(RecommendationResult.fromEntity(saved, RecommendationResult.RecommendationSource.FASTAPI));
                outcome = "completed_pending";
                log.info(
                        "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} outcome={}",
                        traceId, message.memberId(), "future_completed", elapsedMs(consumeStartedAt), outcome
                );
            } else {
                outcome = "stored_without_waiter";
                log.info(
                        "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} outcome={}",
                        traceId, message.memberId(), "no_pending_future", elapsedMs(consumeStartedAt), outcome
                );
            }

            ack.acknowledge();
            log.info(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} topic={} offset={}",
                    traceId, message.memberId(), "ack_done", elapsedMs(consumeStartedAt), topic, offset
            );
        } catch (Exception e) {
            if (memberIdForCleanup != null) {
                CompletableFuture<RecommendationResult> future = pendingFutureRegistry.remove(memberIdForCleanup);
                if (future != null) {
                    future.completeExceptionally(e);
                }
            }
            log.error(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} topic={} offset={} error={}",
                    traceId,
                    memberIdForCleanup != null ? memberIdForCleanup : "unknown",
                    "consume_failed",
                    elapsedMs(consumeStartedAt),
                    topic,
                    offset,
                    e.getMessage(),
                    e
            );
            throw new IllegalStateException("recommendation consume failed", e);
        } finally {
            customerMetrics.stopRecommendationKafkaConsume(timerSample, outcome);
        }
    }

    private long elapsedMs(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000;
    }
}
