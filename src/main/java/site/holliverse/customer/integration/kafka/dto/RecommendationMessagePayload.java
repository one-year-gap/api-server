package site.holliverse.customer.integration.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import site.holliverse.shared.domain.model.PersonaSegment;

import java.util.List;

/**
 * FastAPI가 recommendation-topic으로 발행하는 메시지 스키마.
 */
public record RecommendationMessagePayload(
        @JsonProperty("memberId")
        Long memberId,
        @JsonProperty("trace_id")
        @JsonAlias("traceId")
        String traceId,
        PersonaSegment segment,
        @JsonProperty("cachedLlmRecommendation")
        String cachedLlmRecommendation,
        @JsonProperty("recommendedProducts")
        List<RecommendationProductItemPayload> recommendedProducts
) {
    /** Kafka 메시지 recommendedProducts[] 항목. 내려주는 그대로 (rank, productId, productName, ...). */
    public record RecommendationProductItemPayload(
            Integer rank,
            @JsonProperty("productId")
            Long productId,
            String productName,
            String productType,
            Integer productPrice,
            Integer salePrice,
            List<String> tags,
            @JsonProperty("reason")
            String reason
    ) {}
}
