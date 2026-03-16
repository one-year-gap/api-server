package site.holliverse.admin.query.dao;

import java.time.LocalDate;

/**
 * 이탈 위험군(HIGH) 일별 인원 수 - ChurnRiskTrendDao 반환용.
 */
public record DailyRiskCount(
        LocalDate baseDate,
        Integer count
) {
}
