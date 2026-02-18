package site.holliverse.customer.web.assembler;

import site.holliverse.customer.application.usecase.compare.ComparisonResultDto;
import site.holliverse.customer.application.usecase.product.ProductDetailResult;
import site.holliverse.customer.web.dto.compare.PlanCompareResponse;
import site.holliverse.customer.web.dto.product.ProductDetailResponse;
import site.holliverse.customer.web.mapper.CompareResponseMapper;
import site.holliverse.customer.web.mapper.ProductResponseMapper;

/**
 * 요금제 비교 결과를 API 응답(PlanCompareResponse)으로 조립.
 * Web 계층에서 DTO 조립·반환을 담당한다.
 */
public class PlanCompareResponseAssembler {

    private final ProductResponseMapper productMapper;
    private final CompareResponseMapper compareMapper;

    public PlanCompareResponseAssembler(ProductResponseMapper productMapper, CompareResponseMapper compareMapper) {
        this.productMapper = productMapper;
        this.compareMapper = compareMapper;
    }

    public PlanCompareResponse assemble(
            ProductDetailResult current,
            ProductDetailResult target,
            ComparisonResultDto comparison) {
        ProductDetailResponse currentPlan = productMapper.toDetailResponse(current);
        ProductDetailResponse targetPlan = productMapper.toDetailResponse(target);
        return new PlanCompareResponse(
                currentPlan,
                targetPlan,
                compareMapper.toComparisonResponse(comparison));
    }
}
