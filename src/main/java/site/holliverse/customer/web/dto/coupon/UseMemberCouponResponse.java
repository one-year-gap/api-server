package site.holliverse.customer.web.dto.coupon;

/**
 * 고객 쿠폰 사용 처리 API 응답 DTO.
 * POST /api/v1/customer/coupons/{memberCouponId}/use 응답 body의 data 필드에 해당한다.
 */
public record UseMemberCouponResponse(
        boolean success,
        int remainingCouponCount,
        String appliedBenefitSummary
) {}
