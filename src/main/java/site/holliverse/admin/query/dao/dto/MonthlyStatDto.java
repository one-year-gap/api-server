package site.holliverse.admin.query.dao.dto;

/**
 * jOOQ DAO에서 집계한 월별 통계 데이터를 UseCase로 전달하기 위한 DTO
 */
public record MonthlyStatDto(
        String yearMonth, // "2025-07", "2026-03" 등
        long joinedCount, // 해당 월 가입자 수
        long leftCount    // 해당 월 탈퇴자(구독 해지) 수
) {
}
