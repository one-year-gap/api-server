package site.holliverse.customer.integration.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import site.holliverse.shared.domain.model.PersonaSegment;

import java.util.List;

/**
 * FastAPI가 recommendation-topic으로 발행하는 메시지 스키마.
 */
public record RecommendationMessagePayload(
        @JsonProperty("memberId")
        Long memberId,
        PersonaSegment segment,
        @JsonProperty("cachedLlmRecommendation")
        String cachedLlmRecommendation,
        @JsonProperty("recommendedProducts")
        List<RecommendationProductItemPayload> recommendedProducts
) {
    public record RecommendationProductItemPayload(
            @JsonProperty("productId")
            Long productId,
            @JsonProperty("llmReason")
            String reason
    ) {}
}
