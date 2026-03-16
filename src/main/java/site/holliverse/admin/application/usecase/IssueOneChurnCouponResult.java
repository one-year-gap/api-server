package site.holliverse.admin.application.usecase;

/**
 ==========================
 * $NAME
 * 회원 1명 쿠폰 발급 처리 결과 표입니다.
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-16
 * ========================== */
public record IssueOneChurnCouponResult(
        boolean issued,
        Long memberId,
        String reason
) {
    public static IssueOneChurnCouponResult issued(Long memberId) {
        return new IssueOneChurnCouponResult(true, memberId, null);
    }

    public static IssueOneChurnCouponResult skipped(Long memberId, String reason) {
        return new IssueOneChurnCouponResult(false, memberId, reason);
    }
}
