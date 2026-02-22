package site.holliverse.customer.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.shared.security.CustomUserDetails;
import site.holliverse.customer.application.usecase.compare.ComparePlansUseCase;
import site.holliverse.customer.application.usecase.product.GetProductDetailUseCase;
import site.holliverse.customer.application.usecase.product.GetProductListUseCase;
import site.holliverse.customer.application.usecase.product.ProductDetailResult;
import site.holliverse.customer.application.usecase.product.ProductListResult;
import site.holliverse.customer.web.assembler.PlanCompareResponseAssembler;
import site.holliverse.customer.web.assembler.ProductListResponseAssembler;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.product.ProductDetailResponse;
import site.holliverse.customer.web.dto.product.ProductListResponse;
import site.holliverse.customer.web.dto.product.compare.PlanCompareResponse;
import site.holliverse.customer.web.mapper.ProductResponseMapper;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/customer/plans")
@Profile("customer")
@RequiredArgsConstructor
public class ProductController {

    private final GetProductListUseCase getProductListUseCase;
    private final GetProductDetailUseCase getProductDetailUseCase;
    private final ProductListResponseAssembler productListResponseAssembler;
    private final PlanCompareResponseAssembler planCompareResponseAssembler;
    private final ProductResponseMapper mapper;
    private final ComparePlansUseCase comparePlansUseCase;

    /**
     * 요금제 비교: "현재 멤버의 모바일 구독 상품" vs "대상 상품(targetPlanId)".
     * (비교는 현재 모바일만 지원)
     */
    @GetMapping("/compare")
    public ApiResponse<PlanCompareResponse> comparePlans(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam Long targetPlanId) {
        if (customUserDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        var result = comparePlansUseCase.execute(customUserDetails.getMemberId(), targetPlanId);
        PlanCompareResponse response = planCompareResponseAssembler.assemble(
                result.currentResult(), result.targetResult(), result.comparison());
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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "0") int bestCount) {
        ProductListResult result = getProductListUseCase.execute(category, page, size, bestCount);
        ProductListResponse response = productListResponseAssembler.assemble(result);
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }
}
