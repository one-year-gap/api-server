package site.holliverse.customer.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import site.holliverse.customer.application.usecase.compare.ComparisonResultDto;
import site.holliverse.customer.application.usecase.compare.PlanComparator;
import site.holliverse.customer.application.usecase.product.GetProductDetailUseCase;
import site.holliverse.customer.application.usecase.product.GetProductListUseCase;
import site.holliverse.customer.application.usecase.product.ProductDetailResult;
import site.holliverse.customer.application.usecase.product.ProductListResult;
import site.holliverse.customer.web.assembler.PlanCompareResponseAssembler;
import site.holliverse.customer.web.assembler.ProductListResponseAssembler;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.compare.PlanCompareResponse;
import site.holliverse.customer.web.dto.product.ProductDetailResponse;
import site.holliverse.customer.web.dto.product.ProductListResponse;
import site.holliverse.customer.web.mapper.ProductResponseMapper;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/plans")
@Profile("customer")
@RequiredArgsConstructor
public class ProductController {

    private final GetProductListUseCase getProductListUseCase;
    private final GetProductDetailUseCase getProductDetailUseCase;
    private final PlanComparator planComparator;
    private final ProductListResponseAssembler productListResponseAssembler;
    private final PlanCompareResponseAssembler planCompareResponseAssembler;
    private final ProductResponseMapper mapper;

    /**
     * 요금제 비교. 오케스트레이션·DTO 조립은 Web 계층에서 수행 (UseCase 간 호출·트랜잭션 중첩 방지).
     */
    @GetMapping("/compare")
    public ApiResponse<PlanCompareResponse> comparePlans(
            @RequestParam Long currentPlanId,
            @RequestParam Long targetPlanId) {
        ProductDetailResult currentResult = getProductDetailUseCase.execute(currentPlanId);
        ProductDetailResult targetResult = getProductDetailUseCase.execute(targetPlanId);

        if (currentResult.product().productType() != targetResult.product().productType()) {
            throw new IllegalArgumentException("현재 요금제와 비교 대상의 타입이 같아야 합니다.");
        }
        var currentPlan = currentResult.mobilePlan()
                .orElseThrow(() -> new IllegalArgumentException("현재 요금제의 모바일 상세 정보를 찾을 수 없습니다: " + currentPlanId));
        var targetPlan = targetResult.mobilePlan()
                .orElseThrow(() -> new IllegalArgumentException("비교 대상 요금제의 모바일 상세 정보를 찾을 수 없습니다: " + targetPlanId));

        ComparisonResultDto comparison = planComparator.compare(
                currentResult.product(), currentPlan,
                targetResult.product(), targetPlan);

        PlanCompareResponse response = planCompareResponseAssembler.assemble(currentResult, targetResult, comparison);
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }

    @GetMapping("/{planId}")
    public ApiResponse<ProductDetailResponse> getPlanDetail(@PathVariable Long planId) {
        ProductDetailResult result = getProductDetailUseCase.execute(planId);
        ProductDetailResponse response = mapper.toDetailResponse(result);
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }

    @GetMapping
    public ApiResponse<ProductListResponse> getPlanList(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ProductListResult result = getProductListUseCase.execute(category, page, size);
        ProductListResponse response = productListResponseAssembler.assemble(result);
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }
}
