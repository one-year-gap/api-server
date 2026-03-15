package site.holliverse.admin.web.dto.churn;

import java.time.LocalDate;

/**
 * 이탈 위험군 증감 추이 API - 일별 한 건 (date, riskCount, delta).
 */
public record ChurnRiskDailyDto(
        LocalDate date,    // 기준일 (JSON: "yyyy-MM-dd")
        int riskCount,     // 해당일 이탈 위험군(HIGH) 인원 수
        int delta          // 전일 대비 증감 수
) {
}
