package site.holliverse.customer.web.dto.coupon;

import java.util.List;

/**
 * 고객 보유 쿠폰 목록 조회 API 응답 DTO.
 * GET /api/v1/customer/coupons 응답 body의 data 필드에 해당한다.
 */
public record CustomerCouponListResponse(
        List<CustomerCouponItemResponse> coupons
) {}
