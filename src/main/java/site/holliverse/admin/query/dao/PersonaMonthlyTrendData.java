package site.holliverse.admin.query.dao;

/**
 * 페르소나 월별 사용자 수 트렌드 조회 결과용 DTO
 */
public record PersonaMonthlyTrendData(
        String yearMonth,    // 기준 월 (예: "2025-10")
        String personaName,  // 페르소나 이름 (예: SPACE_SHERLOCK)
        long userCount       // 해당 월(마지막 날 기준) 해당 페르소나 유저 수
) {
}