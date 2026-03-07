package site.holliverse.customer.application.usecase.recommendation;

import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.shared.domain.model.PersonaSegment;

import java.time.Instant;
import java.util.List;

/**
 * 추천 조회/갱신 API 결과 (캐시 또는 FastAPI 출처 구분).
 */
public record RecommendationResult(
        PersonaSegment segment,
        String cachedLlmRecommendation,
        List<RecommendedProductItem> recommendedProducts,
        RecommendationSource source,
        Instant updatedAt
) {
    public enum RecommendationSource {
        CACHE,
        FASTAPI
    }
}
