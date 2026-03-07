package site.holliverse.customer.integration.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import site.holliverse.shared.domain.model.PersonaSegment;

import java.util.List;

/**
 * FastAPI 추천 API 응답 (segment, cached_llm_recommendation, recommended_products).
 */
public record FastApiRecommendationResponse(
        PersonaSegment segment,
        @JsonProperty("cached_llm_recommendation")
        String cachedLlmRecommendation,
        @JsonProperty("recommended_products")
        List<FastApiRecommendedProductItem> recommendedProducts
) {}
