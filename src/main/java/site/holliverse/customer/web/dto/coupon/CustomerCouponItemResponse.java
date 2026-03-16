package site.holliverse.customer.web.dto.coupon;

import java.time.LocalDate;

/**
 * 고객 보유 쿠폰 목록 조회 시 개별 쿠폰 카드용 DTO.
 * UI 카드에 그대로 매핑 가능하도록 표시용 필드로 구성한다.
 */
public record CustomerCouponItemResponse(
        Long memberCouponId,
        String categoryLabel,
        String title,
        String subTitle,
        LocalDate expiredDate,
        Boolean usable
) {}
