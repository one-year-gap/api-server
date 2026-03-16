package site.holliverse.customer.web.dto.coupon;

import java.time.LocalDate;

/**
 * 고객 보유 쿠폰 목록 조회 시 개별 쿠폰 카드용 DTO.
 * UI 카드에 그대로 매핑 가능하도록 표시용 필드로 구성한다.
 *
 * DISCOUNT: 숫자 파싱 후 숫자 포맷 + "원 할인" (예: 5000 → "5,000원 할인")
 * DATA: 문자열 그대로 (예: "데이터 5GB", "데이터 20GB")
 *
 */
public record CustomerCouponItemResponse(
        Long memberCouponId,
        String categoryLabel,
        String title,
        String subTitle,
        String benefitText,
        LocalDate expiredDate,
        Boolean usable
) {}
