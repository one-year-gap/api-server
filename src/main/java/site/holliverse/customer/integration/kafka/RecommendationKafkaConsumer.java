package site.holliverse.customer.integration.kafka;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * recommendation-topic 메시지 수신 → persona_recommendation upsert → 대기 중인 CompletableFuture 완료.
 */
@Slf4j
@RequiredArgsConstructor
public class RecommendationKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final PersonaRecommendationRepository personaRecommendationRepository;
    private final RecommendationPendingFutureRegistry pendingFutureRegistry;

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
        try {
            RecommendationMessagePayload message = objectMapper.readValue(
                    payload, RecommendationMessagePayload.class);

            List<RecommendedProductItem> products = message.recommendedProducts() == null
                    ? new ArrayList<>()
                    : message.recommendedProducts().stream()
                    .map(p -> new RecommendedProductItem(p.productId(), p.reason()))
                    .toList();

            String cachedText = message.cachedLlmRecommendation() != null ? message.cachedLlmRecommendation() : "";
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
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("[Kafka][recommendation] consume failed. topic={}, offset={}, raw={}", topic, offset, payload, e);
            throw new IllegalStateException("recommendation consume failed", e);
        }
    }
}
