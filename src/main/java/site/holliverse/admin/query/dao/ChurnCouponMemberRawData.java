package site.holliverse.admin.query.dao;

/**
 ==========================
 * $NAME
 * 쿠폰 발송 대상 회원 1명을 DB에서 조회했을 때 담는 원본 데이터 객체
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-16
 * ========================== */
public record ChurnCouponMemberRawData(
        Long memberId,
        String role
) {
}
