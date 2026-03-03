package site.holliverse.admin.web.dto.support;

public record AdminSupportStatResponseDto(
        long totalCount,       // 총 상담 건수
        long openCount,        // 미처리 (OPEN) 건수
        long supportingCount,  // 진행중 (SUPPORTING) 건수
        long closedCount       // 완료 (CLOSED) 건수
) {
}