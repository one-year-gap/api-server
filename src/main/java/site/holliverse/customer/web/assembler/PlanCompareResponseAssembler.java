package site.holliverse.customer.web.assembler;

import site.holliverse.customer.application.usecase.PlanCompareResult;
import site.holliverse.customer.web.dto.product.PlanCompareResponse;
import site.holliverse.customer.web.dto.product.ProductDetailResponse;
import site.holliverse.customer.web.mapper.ProductResponseMapper;

/**
 * 요금제 비교 UseCase 결과(PlanCompareResult)를 API 응답(PlanCompareResponse)으로 조립.
 * Mapper는 1:1 변환만 담당하고, 여러 부분을 합쳐 하나의 응답으로 만드는 역할은 Assembler가 담당한다.
 */
public class PlanCompareResponseAssembler {

    private final ProductResponseMapper mapper;

    public PlanCompareResponseAssembler(ProductResponseMapper mapper) {
        this.mapper = mapper;
    }

    public PlanCompareResponse assemble(PlanCompareResult result) {
        ProductDetailResponse currentPlan = mapper.toDetailResponse(result.current());
        ProductDetailResponse targetPlan = mapper.toDetailResponse(result.target());
        return new PlanCompareResponse(
                currentPlan,
                targetPlan,
                mapper.toComparisonResponse(result.comparison()));
    }
}
