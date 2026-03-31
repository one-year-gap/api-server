package site.holliverse.customer.application.usecase.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.integration.kafka.dto.RecommendationMessagePayload;
import site.holliverse.customer.persistence.entity.PersonaRecommendation;
import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.customer.persistence.repository.PersonaRecommendationRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * recommendation-topic 메시지의 도메인 처리 담당.
 * - persona_recommendation upsert
 * - 대기 중인 Future 완료
 */
@Service
@Profile("customer")
@RequiredArgsConstructor
public class RecommendationKafkaConsumeUseCase {

    private final PersonaRecommendationRepository personaRecommendationRepository;
    private final RecommendationPendingFutureRegistry pendingFutureRegistry;

    @Transactional
    public RecommendationKafkaConsumeResult execute(RecommendationMessagePayload message) {
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

        String outcome = "stored_without_waiter";
        CompletableFuture<RecommendationResult> future = pendingFutureRegistry.remove(message.memberId());
        if (future != null) {
            future.complete(RecommendationResult.fromEntity(saved, RecommendationResult.RecommendationSource.FASTAPI));
            outcome = "completed_pending";
        }

        return new RecommendationKafkaConsumeResult(
                message.memberId(),
                message.segment().name(),
                products.size(),
                saved.getUpdatedAt(),
                outcome
        );
    }

    public void completeExceptionally(Long memberId, Exception e) {
        if (memberId == null) {
            return;
        }
        CompletableFuture<RecommendationResult> future = pendingFutureRegistry.remove(memberId);
        if (future != null) {
            future.completeExceptionally(e);
        }
    }

    public record RecommendationKafkaConsumeResult(
            Long memberId,
            String segment,
            int productCount,
            Instant updatedAt,
            String outcome
    ) {}
}
