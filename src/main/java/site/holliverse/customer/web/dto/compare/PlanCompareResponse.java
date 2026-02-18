package site.holliverse.customer.web.dto.compare;

import com.fasterxml.jackson.annotation.JsonProperty;
import site.holliverse.customer.web.dto.product.ProductDetailResponse;

/**
 * 요금제 비교 API 응답 body (옵션 A: current/target는 상세 API와 동일 구조).
 */
public record PlanCompareResponse(
        @JsonProperty("current_plan") ProductDetailResponse currentPlan,
        @JsonProperty("target_plan") ProductDetailResponse targetPlan,
        ComparisonResponse comparison
) {
}
