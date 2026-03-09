package site.holliverse.customer.integration.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * FastAPI 추천 API 요청 body (member_id).
 */
public record FastApiRecommendationRequest(
        @JsonProperty("member_id")
        Long memberId
) {}
