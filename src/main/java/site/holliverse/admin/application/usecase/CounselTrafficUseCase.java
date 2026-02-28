package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.CounselDao;
import site.holliverse.admin.query.dao.CounselTrafficDailyRawData;
import site.holliverse.admin.query.dao.CounselTrafficMonthlyRawData;


import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Profile("admin")
@Service
@RequiredArgsConstructor
public class CounselTrafficUseCase {
    private final CounselDao counselDao;

    /**
     * 시간별 상담 트래픽 조회
     * @param date 'yyyy-MM-dd'
     * @return CounselTrafficRawData
     *          hour:시간대
     *          count: 상담 건 수
     */
    @Transactional(readOnly = true)
    public List<CounselTrafficDailyRawData> getHourlyTraffic(LocalDate date) {
        List<CounselTrafficDailyRawData> rawData = counselDao.fetchCounselTrafficByHour(date);

        int[] countsByHour = new int[24];
        for (CounselTrafficDailyRawData row : rawData) {
            int hour = row.hour();
            if (hour >= 0 && hour < 24) {
                countsByHour[hour] = row.count();
            }
        }

        List<CounselTrafficDailyRawData> result = new ArrayList<>(24);
        for (int hour = 0; hour < 24; hour++) {
            result.add(new CounselTrafficDailyRawData(hour, countsByHour[hour]));
        }

        return result;
    }


    /**
     * 일별 상담 트래픽 조회
     * @param month 'yyyy-MM'
     * @return CounselTrafficRawData
     *          day:일자
     *          count: 상담 건 수
     */
    @Transactional(readOnly = true)
    public List<CounselTrafficMonthlyRawData> getDailyTraffic(YearMonth month) {
        List<CounselTrafficMonthlyRawData> rawData = counselDao.fetchCounselTrafficByDay(month);
        int lastDay = month.lengthOfMonth();
        int[] countsByDay = new int[lastDay + 1];

        for (CounselTrafficMonthlyRawData row : rawData) {
            int day = row.day();
            if (day >= 1 && day <= lastDay) {
                countsByDay[day] = row.count();
            }
        }

        List<CounselTrafficMonthlyRawData> result = new ArrayList<>(lastDay);
        for (int day = 1; day <= lastDay; day++) {
            result.add(new CounselTrafficMonthlyRawData(day, countsByDay[day]));
        }

        return result;
    }
}
