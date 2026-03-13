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
        Long memberIdForCleanup = null;
        try {
            // 수신 로그
            log.debug("[Kafka][recommendation] received. topic={}, offset={}, raw={}", topic, offset, payload);

            RecommendationMessagePayload message = objectMapper.readValue(
                    payload, RecommendationMessagePayload.class);

            memberIdForCleanup = message.memberId();

            List<RecommendedProductItem> products = message.recommendedProducts() == null
                    ? new ArrayList<>()
                    : message.recommendedProducts().stream()
                    .map(p -> new RecommendedProductItem(
                            p.rank(),
                            p.productId(),
                            p.productName(),
                            p.productType(),
                            p.productPrice(),
                            p.salePrice(),
                            p.tags() != null ? p.tags() : new ArrayList<>(),
                            p.reason()
                    ))
                    .toList();

            String cachedText = message.cachedLlmRecommendation() != null ? message.cachedLlmRecommendation() : "";

            // upsert 요약 로그
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
                log.debug(
                        "[Kafka][recommendation] future completed. memberId={}, updatedAt={}",
                        message.memberId(), saved.getUpdatedAt()
                );
            } else {
                log.debug(
                        "[Kafka][recommendation] no pending future. memberId={}, updatedAt={}",
                        message.memberId(), saved.getUpdatedAt()
                );
            }

            ack.acknowledge();
            log.debug("[Kafka][recommendation] acked. topic={}, offset={}", topic, offset);
        } catch (Exception e) {
            if (memberIdForCleanup != null) {
                CompletableFuture<RecommendationResult> future = pendingFutureRegistry.remove(memberIdForCleanup);
                if (future != null) {
                    future.completeExceptionally(e);
                }
            }
            log.error("[Kafka][recommendation] consume failed. topic={}, offset={}, raw={}", topic, offset, payload, e);
            throw new IllegalStateException("recommendation consume failed", e);
        }
    }
}

