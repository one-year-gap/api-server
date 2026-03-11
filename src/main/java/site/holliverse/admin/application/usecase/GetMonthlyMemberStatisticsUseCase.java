package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.MemberStatisticsDao;
import site.holliverse.admin.query.dao.dto.MonthlyStatDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 최근 9개월 월별 가입자/탈퇴자 통계 조회 UseCase
 */
@Service
@RequiredArgsConstructor
public class GetMonthlyMemberStatisticsUseCase {

    private final MemberStatisticsDao statisticsDao;
    private static final int MONTHS_TO_FETCH = 9;

    @Transactional(readOnly = true)
    public List<MonthlyStatDto> execute() {
        // 1. 기준일 계산 (현재 달 포함 최근 9개월 전의 1일)
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(MONTHS_TO_FETCH - 1);
        LocalDate startDate = startMonth.atDay(1);

        // 2. DAO 호출
        Map<String, Long> joinedData = statisticsDao.getJoinedCountByMonth(startDate);
        Map<String, Long> leftData = statisticsDao.getLeftCountByMonth(startDate);

        // 3. 9개월 치의 빈칸(Count 0)을 채우면서 데이터 병합
        List<MonthlyStatDto> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 0; i < MONTHS_TO_FETCH; i++) {
            YearMonth targetMonth = startMonth.plusMonths(i);
            String monthStr = targetMonth.format(formatter); // "2025-07"

            long joinedCount = joinedData.getOrDefault(monthStr, 0L);
            long leftCount = leftData.getOrDefault(monthStr, 0L);

            result.add(new MonthlyStatDto(monthStr, joinedCount, leftCount));
        }

        return result;
    }
}