package site.holliverse.admin.web.assembler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.holliverse.admin.application.usecase.RetrieveRegionalTopPlanUseCase;
import site.holliverse.admin.web.dto.analytics.AdminRegionalTopPlanResponseDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminRegionalTopPlanAssemblerTest {

    private final AdminRegionalTopPlanAssembler assembler = new AdminRegionalTopPlanAssembler();

    @Test
    @DisplayName("전지역 고정 17개로 응답하며 누락 지역은 0/빈배열로 채운다.")
    void toResponse_success() {
        List<RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary> summaries = List.of(
                new RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary(
                        "서울특별시", 500L, List.of("PLAN_A", "PLAN_B", "PLAN_C")
                ),
                new RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary(
                        "부산광역시", 200L, List.of("PLAN_X")
                )
        );

        AdminRegionalTopPlanResponseDto result = assembler.toResponse(summaries);

        assertThat(result.regions()).hasSize(17);
        assertThat(result.regions().get(0).region()).isEqualTo("서울특별시");
        assertThat(result.regions().get(0).regionalSubscriberCount()).isEqualTo(500L);
        assertThat(result.regions().get(0).topPlans()).hasSize(3);
        assertThat(result.regions().get(0).topPlans().get(0).planName()).isEqualTo("PLAN_A");

        // 입력에 없는 지역은 기본값(0/빈배열)로 보정
        assertThat(result.regions().get(1).region()).isEqualTo("인천광역시");
        assertThat(result.regions().get(1).regionalSubscriberCount()).isEqualTo(0L);
        assertThat(result.regions().get(1).topPlans()).isEmpty();
    }
}
