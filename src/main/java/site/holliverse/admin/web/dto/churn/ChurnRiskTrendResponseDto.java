package site.holliverse.admin.web.dto.churn;

import java.util.List;

/**
 * 이탈 위험군 증감 추이 API - 전체 응답 (summary + data).
 */
public record ChurnRiskTrendResponseDto(
        ChurnRiskTrendSummaryDto summary,   // maxIncrease, maxDecrease
        List<ChurnRiskDailyDto> data       // 날짜 오름차순 일별 목록
) {
}
