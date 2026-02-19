package site.holliverse.admin.web.dto.member;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminMemberListResponseDto(
        List<AdminMemberDto> members, // 회원 목록
        Pagination pagination         // 페이징 정보
) {

    public static AdminMemberListResponseDto of(List<AdminMemberDto> members, int totalCount, int page, int size) {
        return AdminMemberListResponseDto.builder()
                .members(members)
                .pagination(Pagination.builder()
                        .totalCount(totalCount)
                        .currentPage(page)
                        .size(size)
                        .totalPage((int) Math.ceil((double) totalCount / size))
                        .build())
                .build();
    }

    @Builder
    public record Pagination(
            int totalCount,
            int currentPage,
            int size,
            int totalPage
    ) {}
}