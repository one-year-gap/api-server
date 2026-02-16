package site.holliverse.customer.application.usecase.compare;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import site.holliverse.customer.application.usecase.product.GetProductDetailUseCase;
import site.holliverse.customer.application.usecase.product.ProductDetailResult;
import site.holliverse.shared.domain.model.ProductType;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComparePlansUseCase 테스트")
class ComparePlansUseCaseTest {

    @Mock
    private GetProductDetailUseCase getProductDetailUseCase;
    @Mock
    private PlanComparator planComparator;

    @InjectMocks
    private ComparePlansUseCase comparePlansUseCase;

    private static ProductDetailResult mobileResult(
            ProductSummaryDto summary,
            Optional<site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto> mobilePlan) {
        return new ProductDetailResult(
                summary,
                mobilePlan,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("같은 타입(모바일)이면 PlanCompareResult 반환하고 comparator 호출")
        void sameType_returnsResultAndCallsComparator() {
            ProductDetailResult current = mobileResult(
                    PlanComparatorTestData.essentialSummary(),
                    Optional.of(PlanComparatorTestData.essentialMobilePlan()));
            ProductDetailResult target = mobileResult(
                    PlanComparatorTestData.plusSummary(),
                    Optional.of(PlanComparatorTestData.plusMobilePlan()));
            ComparisonResultDto comparison = new ComparisonResultDto(15_000, "+15,000원", List.of());

            given(getProductDetailUseCase.execute(1L)).willReturn(current);
            given(getProductDetailUseCase.execute(2L)).willReturn(target);
            given(planComparator.compare(any(), any(), any(), any())).willReturn(comparison);

            PlanCompareResult result = comparePlansUseCase.execute(1L, 2L);

            assertThat(result.current()).isEqualTo(current);
            assertThat(result.target()).isEqualTo(target);
            assertThat(result.comparison()).isEqualTo(comparison);
            verify(getProductDetailUseCase).execute(1L);
            verify(getProductDetailUseCase).execute(2L);
            verify(planComparator).compare(
                    eq(PlanComparatorTestData.essentialSummary()),
                    eq(PlanComparatorTestData.essentialMobilePlan()),
                    eq(PlanComparatorTestData.plusSummary()),
                    eq(PlanComparatorTestData.plusMobilePlan()));
        }

        @Test
        @DisplayName("타입이 다르면 예외")
        void differentType_throws() {
            ProductSummaryDto internetSummary = new ProductSummaryDto(
                    10L, "인터넷상품", 50_000, 45_000, ProductType.INTERNET, "INT-1", "할인");
            ProductDetailResult current = mobileResult(
                    PlanComparatorTestData.essentialSummary(),
                    Optional.of(PlanComparatorTestData.essentialMobilePlan()));
            ProductDetailResult target = mobileResult(internetSummary, Optional.empty());

            given(getProductDetailUseCase.execute(1L)).willReturn(current);
            given(getProductDetailUseCase.execute(2L)).willReturn(target);

            assertThatThrownBy(() -> comparePlansUseCase.execute(1L, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("현재 요금제와 비교 대상의 타입이 같아야 합니다.");
        }

        @Test
        @DisplayName("모바일 상세 없으면 예외")
        void mobileDetailMissing_throws() {
            ProductDetailResult current = mobileResult(
                    PlanComparatorTestData.essentialSummary(),
                    Optional.of(PlanComparatorTestData.essentialMobilePlan()));
            ProductDetailResult targetNoDetail = mobileResult(
                    PlanComparatorTestData.plusSummary(),
                    Optional.empty());

            given(getProductDetailUseCase.execute(1L)).willReturn(current);
            given(getProductDetailUseCase.execute(2L)).willReturn(targetNoDetail);

            assertThatThrownBy(() -> comparePlansUseCase.execute(1L, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비교 대상 요금제의 모바일 상세 정보를 찾을 수 없습니다");
        }
    }
}
