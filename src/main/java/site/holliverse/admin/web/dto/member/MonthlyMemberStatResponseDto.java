package site.holliverse.admin.web.dto.member;

/**
 * 프론트엔드(Admin 대시보드)로 내려주는 최종 응답 DTO
 */
public record MonthlyMemberStatResponseDto(
        String month,     // "YYYY-MM"
        long joinedCount,
        long leftCount
) {
}