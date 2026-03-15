package site.holliverse.admin.web.dto.churn;

/**
 * 이탈 위험군 증감 추이 API - 요약 (기간 내 최대 증감).
 */
public record ChurnRiskTrendSummaryDto(
        int maxIncrease,   // 기간 내 전일 대비 최대 양수 증감
        int maxDecrease    // 기간 내 전일 대비 최대 음수 증감 (예: -4)
) {
}
