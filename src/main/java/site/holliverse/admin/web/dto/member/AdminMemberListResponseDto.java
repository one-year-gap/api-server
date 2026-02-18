package site.holliverse.admin.web.dto.member;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminMemberListResponseDto {
    private List<AdminMemberDto> members; // 회원 목록
    private Pagination pagination;        // 페이징 정보

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

    @Getter
    @Builder
    public static class Pagination {
        private int totalCount;
        private int currentPage;
        private int size;
        private int totalPage;
    }
}