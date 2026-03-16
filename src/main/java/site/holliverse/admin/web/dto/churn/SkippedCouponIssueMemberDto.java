package site.holliverse.admin.web.dto.churn;


import lombok.Builder;

/**
 * ==========================
 * $NAME
 * 발급 생략된 멤버와 이유
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-16
 * ========================== */
@Builder
public record SkippedCouponIssueMemberDto(
        Long memberId,
        String reason
) {
}
