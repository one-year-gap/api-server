package site.holliverse.admin.query.dao;

/**
 * 관리자 대시보드 - 전체 상담 처리 현황 통계 조회 결과를 담는 RowData
 */
public record AdminSupportStatRowData(
        long totalCount,       // 총 상담 건수
        long openCount,        // 미처리 (OPEN) 건수
        long supportingCount,  // 진행중 (SUPPORTING) 건수
        long closedCount       // 완료 (CLOSED) 건수
) {
}