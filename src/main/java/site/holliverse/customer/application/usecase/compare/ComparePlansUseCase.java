package site.holliverse.customer.application.usecase.compare;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import site.holliverse.customer.application.usecase.product.GetProductDetailUseCase;
import site.holliverse.customer.application.usecase.product.ProductDetailResult;

/**
 * 요금제 비교 UseCase. 현재는 모바일 요금제 비교만 구현되어 있으며, 동일 타입끼리만 비교 가능.
 */
@Service
public class ComparePlansUseCase {

    private final GetProductDetailUseCase getProductDetailUseCase;
    private final PlanComparator planComparator;

    public ComparePlansUseCase(GetProductDetailUseCase getProductDetailUseCase,
                              PlanComparator planComparator) {
        this.getProductDetailUseCase = getProductDetailUseCase;
        this.planComparator = planComparator;
    }

    @Transactional(readOnly = true)
    public PlanCompareResult execute(Long currentPlanId, Long targetPlanId) {
        ProductDetailResult currentResult = getProductDetailUseCase.execute(currentPlanId);
        ProductDetailResult targetResult = getProductDetailUseCase.execute(targetPlanId);

        ProductSummaryDto currentSummary = currentResult.product();
        ProductSummaryDto targetSummary = targetResult.product();

        if (currentSummary.productType() != targetSummary.productType()) {
            throw new IllegalArgumentException("현재 요금제와 비교 대상의 타입이 같아야 합니다.");
        }

        MobilePlanDetailDto currentPlan = currentResult.mobilePlan()
                .orElseThrow(() -> new IllegalArgumentException("현재 요금제의 모바일 상세 정보를 찾을 수 없습니다: " + currentPlanId));
        MobilePlanDetailDto targetPlan = targetResult.mobilePlan()
                .orElseThrow(() -> new IllegalArgumentException("비교 대상 요금제의 모바일 상세 정보를 찾을 수 없습니다: " + targetPlanId));

        ComparisonResultDto comparison = planComparator.compare(
                currentSummary, currentPlan,
                targetSummary, targetPlan);

        return new PlanCompareResult(currentResult, targetResult, comparison);
    }
}
