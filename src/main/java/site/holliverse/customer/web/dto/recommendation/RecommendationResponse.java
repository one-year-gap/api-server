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
    /** 추천 상품 한 건. 내려주는 그대로 (rank, productId, productName, productType, productPrice, salePrice, tags, reason). */
    public record RecommendationProductItem(
            Integer rank,
            Long productId,
            String productName,
            String productType,
            Integer productPrice,
            Integer salePrice,
            List<String> tags,
            String reason
    ) {}
}
