package site.holliverse.admin.web.dto.churn;

import lombok.Builder;

import java.util.List;

@Builder
public record ChurnRiskMemberListResponseDto(
        List<ChurnRiskMemberDto> members,
        Pagination pagination
) {
    public static ChurnRiskMemberListResponseDto of(List<ChurnRiskMemberDto> members, int totalCount, int page, int size) {
        return ChurnRiskMemberListResponseDto.builder()
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
    ) {
    }
}
