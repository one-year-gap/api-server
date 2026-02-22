package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.query.dao.AdminRegionalMetricDao;
import site.holliverse.admin.query.dao.RegionalMetricRawData;
import site.holliverse.admin.web.dto.analytics.AdminRegionalMetricRequestDto;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RetrieveRegionalMetricUseCaseTest {

    @Mock
    private AdminRegionalMetricDao adminRegionalMetricDao;

    @InjectMocks
    private RetrieveRegionalMetricUseCase retrieveRegionalMetricUseCase;

    @Test
    @DisplayName("지역별 집계 데이터를 그대로 반환한다.")
    void execute_success() {
        AdminRegionalMetricRequestDto requestDto = new AdminRegionalMetricRequestDto("202601");
        List<RegionalMetricRawData> mock = List.of(
                new RegionalMetricRawData("서울", BigDecimal.valueOf(45454), BigDecimal.valueOf(12999))
        );
        given(adminRegionalMetricDao.findRegionalAverages("202601")).willReturn(mock);

        List<RegionalMetricRawData> result = retrieveRegionalMetricUseCase.execute(requestDto);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).province()).isEqualTo("서울");
        verify(adminRegionalMetricDao).findRegionalAverages("202601");
    }
}
