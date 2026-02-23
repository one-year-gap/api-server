package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.query.dao.AdminRegionalTopPlanDao;
import site.holliverse.admin.query.dao.RegionalSubscriberCountRawData;
import site.holliverse.admin.query.dao.RegionalTopPlanRawData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RetrieveRegionalTopPlanUseCaseTest {

    @Mock
    private AdminRegionalTopPlanDao adminRegionalTopPlanDao;

    @InjectMocks
    private RetrieveRegionalTopPlanUseCase retrieveRegionalTopPlanUseCase;

    @Test
    @DisplayName("전지역 요약(총 가입자 수 + Top3 요금제)을 조합해 반환한다.")
    void execute_success() {
        given(adminRegionalTopPlanDao.findSubscriberCountsByAllProvinces()).willReturn(List.of(
                new RegionalSubscriberCountRawData("SEOUL", 500L),
                new RegionalSubscriberCountRawData("BUSAN", 200L)
        ));
        given(adminRegionalTopPlanDao.findTopPlansByAllProvinces(3)).willReturn(List.of(
                new RegionalTopPlanRawData("SEOUL", "PLAN_A"),
                new RegionalTopPlanRawData("SEOUL", "PLAN_B"),
                new RegionalTopPlanRawData("SEOUL", "PLAN_C"),
                new RegionalTopPlanRawData("BUSAN", "PLAN_X")
        ));

        List<RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary> result = retrieveRegionalTopPlanUseCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).province()).isEqualTo("SEOUL");
        assertThat(result.get(0).regionalSubscriberCount()).isEqualTo(500L);
        assertThat(result.get(0).topPlanNames()).containsExactly("PLAN_A", "PLAN_B", "PLAN_C");
        assertThat(result.get(1).province()).isEqualTo("BUSAN");
        assertThat(result.get(1).topPlanNames()).containsExactly("PLAN_X");
        verify(adminRegionalTopPlanDao).findSubscriberCountsByAllProvinces();
        verify(adminRegionalTopPlanDao).findTopPlansByAllProvinces(3);
    }
}
