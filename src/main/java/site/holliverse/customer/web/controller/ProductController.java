package site.holliverse.customer.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.customer.application.usecase.ComparePlansUseCase;
import site.holliverse.customer.application.usecase.GetProductDetailUseCase;
import site.holliverse.customer.application.usecase.GetProductListUseCase;
import site.holliverse.customer.application.usecase.PlanCompareResult;
import site.holliverse.customer.application.usecase.ProductDetailResult;
import site.holliverse.customer.application.usecase.ProductListResult;
import site.holliverse.customer.web.assembler.PlanCompareResponseAssembler;
import site.holliverse.customer.web.assembler.ProductListResponseAssembler;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.product.PlanCompareResponse;
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
    private final ComparePlansUseCase comparePlansUseCase;
    private final ProductListResponseAssembler productListResponseAssembler;
    private final PlanCompareResponseAssembler planCompareResponseAssembler;
    private final ProductResponseMapper mapper;

    @GetMapping("/compare")
    public ApiResponse<PlanCompareResponse> comparePlans(
            @RequestParam Long currentPlanId,
            @RequestParam Long targetPlanId) {
        PlanCompareResult result = comparePlansUseCase.execute(currentPlanId, targetPlanId);
        PlanCompareResponse response = planCompareResponseAssembler.assemble(result);
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
