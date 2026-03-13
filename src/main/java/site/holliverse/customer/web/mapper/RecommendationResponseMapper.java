package site.holliverse.customer.web.mapper;

import site.holliverse.customer.application.usecase.recommendation.RecommendationResult;
import site.holliverse.customer.web.dto.recommendation.RecommendationResponse;

import java.util.Collections;
import java.util.List;

/**
 * RecommendationResult → RecommendationResponse 변환.
 */
public class RecommendationResponseMapper {

    public RecommendationResponse toResponse(RecommendationResult result) {
        List<RecommendationResponse.RecommendationProductItem> items = result.recommendedProducts().stream()
                .map(item -> new RecommendationResponse.RecommendationProductItem(
                        item.rank(),
                        item.productId(),
                        item.productName(),
                        item.productType(),
                        item.productPrice(),
                        item.salePrice(),
                        item.tags() != null ? item.tags() : Collections.emptyList(),
                        item.reason()
                ))
                .toList();
        return new RecommendationResponse(
                result.segment(),
                result.cachedLlmRecommendation(),
                items,
                result.source(),
                result.updatedAt()
        );
    }
}
