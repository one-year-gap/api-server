package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.query.dao.AdminMembershipStatDao;
import site.holliverse.admin.query.dao.AdminMembershipStatRawData;
import site.holliverse.admin.web.dto.member.TotalMembershipResponseDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetMembershipCountUseCaseTest {

    @Mock
    private AdminMembershipStatDao adminMembershipStatDao;

    @InjectMocks
    private GetMembershipCountUseCase getMembershipCountUseCase;

    @Test
    @DisplayName("total이 0이면 모든 비율과 totalInK를 0으로 반환한다")
    void execute_whenTotalIsZero_returnsZeroValues() {
        // given
        given(adminMembershipStatDao.getMembershipStats())
                .willReturn(new AdminMembershipStatRawData(0L, 0L, 0L, 0L));

        // when
        TotalMembershipResponseDto result = getMembershipCountUseCase.execute();

        // then
        assertThat(result.totalInK()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.vvipRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.vipRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.goldRate()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(adminMembershipStatDao).getMembershipStats();
    }

    @Test
    @DisplayName("정상 케이스에서 totalInK와 비율을 계산한다")
    void execute_whenNormalData_returnsCalculatedRates() {
        // given: total=1000, vvip=700, vip=200, gold=100
        given(adminMembershipStatDao.getMembershipStats())
                .willReturn(new AdminMembershipStatRawData(1000L, 700L, 200L, 100L));

        // when
        TotalMembershipResponseDto result = getMembershipCountUseCase.execute();

        // then
        assertThat(result.totalInK()).isEqualByComparingTo("1.0");
        assertThat(result.vvipRate()).isEqualByComparingTo("70.0");
        assertThat(result.vipRate()).isEqualByComparingTo("20.0");
        assertThat(result.goldRate()).isEqualByComparingTo("10.0");
    }

    @Test
    @DisplayName("반올림 보정으로 비율 합계를 100.0으로 맞춘다")
    void execute_roundingCorrection_makesSumExactlyHundred() {
        // given: 1/3,1/3,1/3 케이스 -> 33.4,33.3,33.3 합이 100이 되어야한다.
        given(adminMembershipStatDao.getMembershipStats())
                .willReturn(new AdminMembershipStatRawData(3L, 1L, 1L, 1L));

        // when
        TotalMembershipResponseDto result = getMembershipCountUseCase.execute();

        // then
        BigDecimal sum = result.vvipRate().add(result.vipRate()).add(result.goldRate());
        assertThat(sum).isEqualByComparingTo("100.0");
        assertThat(result.vvipRate()).isEqualByComparingTo("33.4");
        assertThat(result.vipRate()).isEqualByComparingTo("33.3");
        assertThat(result.goldRate()).isEqualByComparingTo("33.3");
    }
}

