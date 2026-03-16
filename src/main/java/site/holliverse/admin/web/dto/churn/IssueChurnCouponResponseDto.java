package site.holliverse.admin.web.dto.churn;

import lombok.Builder;

import java.util.List;

/**
 ==========================
 * $NAME
 * 쿠폰 발급 응답 dto
 *
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-16
 * ========================== */
@Builder
public record IssueChurnCouponResponseDto(

        //요청된 멤버 카운트
        int requestedCount,

        // 발급된 멤버 카운트
        int issuedCount,

        // 발급 생략된 멤버 카운트
        int skippedCount,

        //발급된 멤버들
        List<Long> issuedMemberIds,
        //발급 생략된 멤버들
        List<SkippedCouponIssueMemberDto> skippedMembers

) {
}
