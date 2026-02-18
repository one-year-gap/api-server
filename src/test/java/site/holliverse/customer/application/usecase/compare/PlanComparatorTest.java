package site.holliverse.customer.application.usecase.compare;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("요금제 비교 엔진 단위 테스트")
class PlanComparatorTest {

    /**
     * 검증 내용
     * 1. 가격 차이 (정확한 계산과 +가격 메시지 확인)
     * 2. 스펙 변경 검증
     * 3. 혜택 추가 (|로 구분된 다수의 브랜드 혜택이 리스트에 누락없이 담기는지 확인)
     */
    private final PlanComparator comparator = new PlanComparator();

    @Nested
    @DisplayName("5G 프리미어 에센셜 → 5G 프리미어 플러스 (실제 데이터)")
    class EssentialToPlus {

        @Test
        @DisplayName("에센셜 대비 플러스 상향 시 가격 차이와 메시지를 정확히 계산한다")
        void priceDiffAndMessage() {
            ProductSummaryDto current = PlanComparatorTestData.essentialSummary();
            MobilePlanDetailDto currentPlan = PlanComparatorTestData.essentialMobilePlan();
            ProductSummaryDto target = PlanComparatorTestData.plusSummary();
            MobilePlanDetailDto targetPlan = PlanComparatorTestData.plusMobilePlan();

            ComparisonResultDto result = comparator.compare(current, currentPlan, target, targetPlan);

            // 73,500 - 58,500 = 15,000원 추가
            assertThat(result.priceDiff()).isEqualTo(15_000);
            assertThat(result.message()).isEqualTo("+15,000원");
        }

        @Test
        @DisplayName("테더링·혜택 등 스펙 변경이 benefit_changes에 반영된다")
        void benefitChanges() {
            ProductSummaryDto current = PlanComparatorTestData.essentialSummary();
            MobilePlanDetailDto currentPlan = PlanComparatorTestData.essentialMobilePlan();
            ProductSummaryDto target = PlanComparatorTestData.plusSummary();
            MobilePlanDetailDto targetPlan = PlanComparatorTestData.plusMobilePlan();

            ComparisonResultDto result = comparator.compare(current, currentPlan, target, targetPlan);

            assertThat(result.benefitChanges()).isNotEmpty();
            // 테더링: 70GB → 100GB 변경
            assertThat(result.benefitChanges().stream().filter(c -> "테더링".equals(c.item())).findFirst())
                    .get().satisfies(c -> {
                        assertThat(c.isChanged()).isTrue();
                        assertThat(c.desc()).contains("70GB").contains("100GB");
                    });
            // 혜택: 에센셜 없음 → 플러스 OTT·미디어·프리미엄 혜택 추가 (브랜드 5종 + 미디어/프리미엄 문구)
            assertThat(result.benefitChanges().stream().filter(c -> "혜택".equals(c.item())).findFirst())
                    .get().satisfies(c -> {
                        assertThat(c.isChanged()).isTrue();
                        assertThat(c.addedBrands()).contains(
                                "넷플릭스", "유튜브 프리미엄", "디즈니+", "티빙", "멀티팩"
                        );
                        assertThat(c.removedBrands()).isEmpty();
                    });
        }
    }
}
