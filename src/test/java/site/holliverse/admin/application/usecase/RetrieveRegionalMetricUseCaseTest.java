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

/**
 * RetrieveRegionalMetricUseCase 단위 테스트.
 *
 * 검증 포인트:
 * - requestDto.yyyymm 값을 DAO 호출 파라미터로 전달하는지
 * - DAO 결과를 가공 없이 그대로 반환하는지
 */
@ExtendWith(MockitoExtension.class)
class RetrieveRegionalMetricUseCaseTest {

    @Mock
    private AdminRegionalMetricDao adminRegionalMetricDao;

    @InjectMocks
    private RetrieveRegionalMetricUseCase retrieveRegionalMetricUseCase;

    @Test
    @DisplayName("요청 yyyymm으로 DAO를 호출하고 결과를 그대로 반환한다.")
    void execute_delegates_to_dao() {
        AdminRegionalMetricRequestDto requestDto = new AdminRegionalMetricRequestDto("202602");
        List<RegionalMetricRawData> expected = List.of(
                new RegionalMetricRawData("서울특별시", BigDecimal.valueOf(200), BigDecimal.valueOf(15))
        );

        given(adminRegionalMetricDao.findRegionalAverages("202602")).willReturn(expected);

        List<RegionalMetricRawData> result = retrieveRegionalMetricUseCase.execute(requestDto);

        assertThat(result).isEqualTo(expected);
        verify(adminRegionalMetricDao).findRegionalAverages("202602");
    }
}