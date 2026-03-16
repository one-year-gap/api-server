package site.holliverse.customer.coupon.domain;

import site.holliverse.shared.domain.model.CouponType;

/**
 * 쿠폰 메타 정보 (도메인 엔티티).
 * 목록/사용 API 응답 매핑 시 혜택 문구·카테고리 라벨 등에 사용한다.
 */
public record Coupon(
        Long couponId,
        String name,
        CouponType couponType,
        String benefitValue,
        String description
) {}
