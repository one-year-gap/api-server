package site.holliverse.customer.coupon.domain;

import java.time.Instant;

/**
 * 회원 보유 쿠폰 애그리거트.
 * 조회·사용 API에서 UseCase가 반환하는 도메인 타입이다.
 */
public record MemberCoupon(
        Long memberCouponId,
        Long memberId,
        Long couponId,
        MemberCouponStatus status,
        Instant issuedAt,
        Instant usedAt,
        Instant expiredAt,
        Coupon coupon
) {}
