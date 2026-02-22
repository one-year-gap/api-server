package site.holliverse.customer.application.usecase.compare;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto;
import site.holliverse.customer.application.usecase.product.ChangeProductUseCase;
import site.holliverse.customer.application.usecase.product.GetProductDetailUseCase;
import site.holliverse.customer.application.usecase.product.ProductDetailResult;

@Service
@Profile("customer")
public class ComparePlansUseCase {

    private final ChangeProductUseCase changeProductUseCase;
    private final GetProductDetailUseCase getProductDetailUseCase;
    private final PlanComparator planComparator;

    public ComparePlansUseCase(ChangeProductUseCase changeProductUseCase,
                               GetProductDetailUseCase getProductDetailUseCase,
                               PlanComparator planComparator) {
        this.changeProductUseCase = changeProductUseCase;
        this.getProductDetailUseCase = getProductDetailUseCase;
        this.planComparator = planComparator;
    }

    @Transactional(readOnly = true)
    public ComparePlansResult execute(Long memberId, Long targetPlanId) {
        Long currentPlanId = changeProductUseCase.findCurrentMobileProductId(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "비교하려면 모바일 요금제를 먼저 가입해 주세요."));

        ProductDetailResult currentResult = getProductDetailUseCase.execute(currentPlanId);
        ProductDetailResult targetResult = getProductDetailUseCase.execute(targetPlanId);

        if (currentResult.product().productType() != targetResult.product().productType()) {
            throw new IllegalArgumentException("현재 요금제와 비교 대상의 타입이 같아야 합니다.");
        }
        MobilePlanDetailDto currentPlan = currentResult.mobilePlan()
                .orElseThrow(() -> new IllegalArgumentException("현재 요금제의 모바일 상세 정보를 찾을 수 없습니다: " + currentPlanId));
        MobilePlanDetailDto targetPlan = targetResult.mobilePlan()
                .orElseThrow(() -> new IllegalArgumentException("비교 대상 요금제의 모바일 상세 정보를 찾을 수 없습니다: " + targetPlanId));

        ComparisonResultDto comparison = planComparator.compare(
                currentResult.product(), currentPlan,
                targetResult.product(), targetPlan);

        return new ComparePlansResult(currentResult, targetResult, comparison);
    }
}
