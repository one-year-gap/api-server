package site.holliverse.customer.integration.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import site.holliverse.shared.domain.model.PersonaSegment;

import java.util.List;

/**
 * FastAPI가 recommendation-topic으로 발행하는 메시지 스키마.
 */
public record RecommendationMessagePayload(
        @JsonProperty("member_id")
        Long memberId,
        PersonaSegment segment,
        @JsonProperty("cached_llm_recommendation")
        String cachedLlmRecommendation,
        @JsonProperty("recommended_products")
        List<RecommendationProductItemPayload> recommendedProducts
) {
    public record RecommendationProductItemPayload(
            @JsonProperty("product_id")
            Long productId,
            String reason
    ) {}
}
