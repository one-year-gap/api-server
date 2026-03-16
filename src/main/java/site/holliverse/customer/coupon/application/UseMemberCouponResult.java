package site.holliverse.customer.coupon.application;

import site.holliverse.customer.coupon.domain.MemberCoupon;

/**
 * 쿠폰 사용 처리 UseCase 반환 타입.
 */
public record UseMemberCouponResult(
        MemberCoupon usedMemberCoupon,
        int remainingCouponCount
) {}
