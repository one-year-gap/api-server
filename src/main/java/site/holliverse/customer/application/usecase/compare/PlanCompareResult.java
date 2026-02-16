package site.holliverse.customer.application.usecase.compare;

import site.holliverse.customer.application.usecase.product.ProductDetailResult;

/**
 * 요금제 비교 UseCase 결과 (current/target는 상세 조회와 동일한 Result 재사용).
 */
public record PlanCompareResult(
        ProductDetailResult current,
        ProductDetailResult target,
        ComparisonResultDto comparison
) {}
