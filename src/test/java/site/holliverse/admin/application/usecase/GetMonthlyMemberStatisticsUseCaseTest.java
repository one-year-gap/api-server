package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.query.dao.MemberStatisticsDao;
import site.holliverse.admin.query.dao.dto.MonthlyStatDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetMonthlyMemberStatisticsUseCaseTest {

    @Mock
    private MemberStatisticsDao statisticsDao;

    @InjectMocks
    private GetMonthlyMemberStatisticsUseCase getMonthlyMemberStatisticsUseCase;

    @Test
    @DisplayName("최근 9개월 통계 조회 시, 데이터가 없는 달은 0으로 채워서 총 9개의 데이터를 반환한다.")
    void execute_fillsMissingMonthsWithZero() {
        // given
        // DB에는 특정 달(이번 달, 3개월 전) 데이터만 있다고 가정
        String currentMonth = YearMonth.now().toString();
        String threeMonthsAgo = YearMonth.now().minusMonths(3).toString();

        given(statisticsDao.getJoinedCountByMonth(any(LocalDate.class)))
                .willReturn(Map.of(currentMonth, 10L, threeMonthsAgo, 5L));

        given(statisticsDao.getLeftCountByMonth(any(LocalDate.class)))
                .willReturn(Map.of(currentMonth, 2L));

        // when
        List<MonthlyStatDto> result = getMonthlyMemberStatisticsUseCase.execute();

        // then
        assertThat(result).hasSize(9); // 항상 9개월치 데이터 보장

        // 이번 달 데이터 검증
        MonthlyStatDto latest = result.get(8);
        assertThat(latest.yearMonth()).isEqualTo(currentMonth);
        assertThat(latest.joinedCount()).isEqualTo(10L);
        assertThat(latest.leftCount()).isEqualTo(2L);

        // 데이터가 없는 달(예: 1개월 전) 검증
        MonthlyStatDto missingMonth = result.get(7);
        assertThat(missingMonth.joinedCount()).isEqualTo(0L);
        assertThat(missingMonth.leftCount()).isEqualTo(0L);
    }
}