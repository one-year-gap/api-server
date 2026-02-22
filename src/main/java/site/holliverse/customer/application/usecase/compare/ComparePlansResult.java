package site.holliverse.customer.application.usecase.compare;

import site.holliverse.customer.application.usecase.product.ProductDetailResult;

/**
 * 요금제 비교 UseCase 실행 결과. Controller에서 Assembler로 넘긴다.
 */
public record ComparePlansResult(
        ProductDetailResult currentResult,
        ProductDetailResult targetResult,
        ComparisonResultDto comparison
) {}
