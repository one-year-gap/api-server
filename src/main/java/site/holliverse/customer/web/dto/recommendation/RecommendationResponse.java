package site.holliverse.customer.web.dto.recommendation;

import site.holliverse.customer.application.usecase.recommendation.RecommendationResult;
import site.holliverse.shared.domain.model.PersonaSegment;

import java.time.Instant;
import java.util.List;

/**
 * 추천 API 응답 (segment, 캐시 문구, 추천 상품 목록, 출처, 갱신 시각).
 */
public record RecommendationResponse(
        PersonaSegment segment,
        String cachedLlmRecommendation,
        List<RecommendationProductItem> recommendedProducts,
        RecommendationResult.RecommendationSource source,
        Instant updatedAt
) {
    public record RecommendationProductItem(Long productId, String reason) {}
}
