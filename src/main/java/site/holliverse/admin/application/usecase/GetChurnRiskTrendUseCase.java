package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.ChurnRiskTrendDao;
import site.holliverse.admin.query.dao.DailyRiskCount;
import site.holliverse.admin.web.dto.churn.ChurnRiskDailyDto;
import site.holliverse.admin.web.dto.churn.ChurnRiskTrendResponseDto;
import site.holliverse.admin.web.dto.churn.ChurnRiskTrendSummaryDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 이탈 위험군 증감 추이 조회.
 * 어제 기준 어제~-31일 구간의 전일 대비 증감·riskCount·summary 반환.
 */
@Service
@Profile("admin")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetChurnRiskTrendUseCase {

    private final ChurnRiskTrendDao churnRiskTrendDao;

    /**
     * 이탈 위험군(HIGH) 일별 인원·전일 대비 증감·기간 내 max 증감을 반환.
     */
    public ChurnRiskTrendResponseDto execute() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate from = yesterday.minusDays(31);
        LocalDate to = yesterday;

        List<DailyRiskCount> dailyCounts = churnRiskTrendDao.findDailyHighRiskCounts(from, to);
        Map<LocalDate, Integer> countByDate = dailyCounts.stream()
                .collect(Collectors.toMap(DailyRiskCount::baseDate, DailyRiskCount::count));

        int maxIncrease = 0;
        int maxDecrease = 0;
        List<ChurnRiskDailyDto> data = new ArrayList<>();

        // d ∈ [yesterday-30, yesterday] (31일)에 대해 riskCount, delta 계산
        for (LocalDate d = yesterday.minusDays(30); !d.isAfter(yesterday); d = d.plusDays(1)) {
            int riskCount = countByDate.getOrDefault(d, 0);
            int prevCount = countByDate.getOrDefault(d.minusDays(1), 0);
            int delta = riskCount - prevCount;

            data.add(new ChurnRiskDailyDto(d, riskCount, delta));

            if (delta > 0) {
                maxIncrease = Math.max(maxIncrease, delta);
            } else if (delta < 0) {
                maxDecrease = Math.min(maxDecrease, delta);
            }
        }

        ChurnRiskTrendSummaryDto summary = new ChurnRiskTrendSummaryDto(maxIncrease, maxDecrease);
        return new ChurnRiskTrendResponseDto(summary, data);
    }
}
