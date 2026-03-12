package site.holliverse.admin.web.dto.analytics;

/**
 * 페르소나 월별 사용자 수 트렌드 응답
 */
public record PersonaMonthlyTrendResponseDto(
        String yearMonth,
        String personaName,
        long userCount
) {
}