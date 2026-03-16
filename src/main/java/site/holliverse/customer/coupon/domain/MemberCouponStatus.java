package site.holliverse.customer.coupon.domain;

/**
 * 회원 보유 쿠폰 상태.
 */
public enum MemberCouponStatus {
    ISSUED,  // 발급됨(미사용)
    USED,    // 사용됨
    EXPIRED  // 만료됨
}
