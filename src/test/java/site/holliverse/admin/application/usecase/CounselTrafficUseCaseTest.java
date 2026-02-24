package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.query.dao.CounselDao;
import site.holliverse.admin.query.dao.CounselTrafficDailyRawData;
import site.holliverse.admin.query.dao.CounselTrafficMonthlyRawData;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CounselTrafficUseCaseTest {
    @InjectMocks
    private CounselTrafficUseCase useCase;

    @Mock
    private CounselDao dao;

    @Test
    @DisplayName("시간 단위 조회한 데이터값이 빈 배열이면 결과 모두 count=0값으로 반환된다.")
    void get_daily_traffic_all_zero_data() {
        //given
        LocalDate date = LocalDate.of(2022, 12, 1);

        //when
        when(dao.fetchCounselTrafficByHour(date)).thenReturn(Collections.emptyList());
        List<CounselTrafficDailyRawData> result = useCase.getHourlyTraffic(date);

        //then
        assertThat(result).hasSize(24);
        for (CounselTrafficDailyRawData data : result) {
            assertThat(data.count()).isEqualTo(0);
        }

    }

    @Test
    @DisplayName("일 단위 조회한 데이터값이 빈 배열이면 결과 모두 count=0값으로 반환된다.")
    void get_monthly_traffic_all_zero_data() {
        // given
        YearMonth month = YearMonth.of(2026, 2);

        // when
        when(dao.fetchCounselTrafficByDay(month)).thenReturn(Collections.emptyList());
        List<CounselTrafficMonthlyRawData> result = useCase.getDailyTraffic(month);

        // then
        assertThat(result).hasSize(28);
        assertThat(result).allSatisfy(d -> assertThat(d.count()).isEqualTo(0));
    }

    @Test
    @DisplayName("시간 단위 조회한 데이터 값이 존재하면 해당 데이터를 제외하고 나머지 데이터의 count=0으로 반환된다.")
    void get_daily_traffic() {
        //given
        LocalDate date = LocalDate.of(2022, 12, 1);
        List<CounselTrafficDailyRawData> rawData = List.of(
                new CounselTrafficDailyRawData(0, 10),
                new CounselTrafficDailyRawData(4, 124),
                new CounselTrafficDailyRawData(13, 12),
                new CounselTrafficDailyRawData(17, 15555)
        );

        //when
        when(dao.fetchCounselTrafficByHour(date)).thenReturn(rawData);
        List<CounselTrafficDailyRawData> result = useCase.getHourlyTraffic(date);

        //then
        assertThat(result).hasSize(24);
        for (CounselTrafficDailyRawData data : result) {
            switch (data.hour()) {
                case 0 -> assertThat(data.count()).isEqualTo(10);
                case 4 -> assertThat(data.count()).isEqualTo(124);
                case 13 -> assertThat(data.count()).isEqualTo(12);
                case 17 -> assertThat(data.count()).isEqualTo(15555);
                default -> assertThat(data.count()).isEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("일 단위 조회한 데이터 값이 존재하면 해당 데이터를 제외하고 나머지 데이터의 count=0으로 반환된다.")
    void get_monthly_traffic() {
        //given
        YearMonth date = YearMonth.of(2022, 12);
        List<CounselTrafficMonthlyRawData> rawData = List.of(
                new CounselTrafficMonthlyRawData(1, 100),
                new CounselTrafficMonthlyRawData(3, 14),
                new CounselTrafficMonthlyRawData(23, 2),
                new CounselTrafficMonthlyRawData(27, 315555)
        );

        //when
        when(dao.fetchCounselTrafficByDay(date)).thenReturn(rawData);
        List<CounselTrafficMonthlyRawData> result = useCase.getDailyTraffic(date);

        //then
        assertThat(result).hasSize(31);
        for (CounselTrafficMonthlyRawData data : result) {
            switch (data.day()) {
                case 1 -> assertThat(data.count()).isEqualTo(100);
                case 3 -> assertThat(data.count()).isEqualTo(14);
                case 23 -> assertThat(data.count()).isEqualTo(2);
                case 27 -> assertThat(data.count()).isEqualTo(315555);
                default -> assertThat(data.count()).isEqualTo(0);
            }
        }
    }
}
