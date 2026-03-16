package site.holliverse.customer.coupon.web;

import site.holliverse.customer.coupon.application.UseMemberCouponResult;
import site.holliverse.customer.coupon.domain.Coupon;
import site.holliverse.customer.coupon.domain.MemberCoupon;
import site.holliverse.shared.domain.model.CouponType;
import site.holliverse.customer.web.dto.coupon.CustomerCouponItemResponse;
import site.holliverse.customer.web.dto.coupon.CustomerCouponListResponse;
import site.holliverse.customer.web.dto.coupon.UseMemberCouponResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 도메인(MemberCoupon, Coupon) → 고객 쿠폰 API Response DTO 변환.
 * Repository 호출 및 lazy loading 트리거 금지.
 */
public class CustomerCouponMapper {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    /**
     * 보유 쿠폰 목록 도메인 → 목록 응답 DTO.
     * - title ← coupon.description
     * - categoryLabel ← couponType: DISCOUNT "요금 할인", DATA "데이터"
     * - benefitText ← DISCOUNT: 숫자포맷 + "원 할인", DATA: benefitValue 그대로
     */
    public CustomerCouponListResponse toListResponse(List<MemberCoupon> coupons) {
        List<CustomerCouponItemResponse> items = coupons.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
        return new CustomerCouponListResponse(items);
    }

    public CustomerCouponItemResponse toItemResponse(MemberCoupon memberCoupon) {
        Coupon coupon = memberCoupon.coupon();
        return new CustomerCouponItemResponse(
                memberCoupon.memberCouponId(),
                toCategoryLabel(coupon.couponType()),
                coupon.description(),
                toSubTitle(coupon),
                toBenefitText(coupon),
                toExpiredDate(memberCoupon.expiredAt()),
                true
        );
    }

    /**
     * 쿠폰 사용 처리 결과 → 사용 응답 DTO.
     */
    public UseMemberCouponResponse toUseResponse(UseMemberCouponResult result) {
        String appliedBenefitSummary = result.usedMemberCoupon().coupon() != null
                ? toBenefitText(result.usedMemberCoupon().coupon())
                : "";
        return new UseMemberCouponResponse(
                true,
                result.remainingCouponCount(),
                appliedBenefitSummary
        );
    }

    private static String toCategoryLabel(CouponType type) {
        return type == CouponType.DISCOUNT ? "요금 할인" : "데이터";
    }

    private static String toSubTitle(Coupon coupon) {
        if (coupon.couponType() == CouponType.DISCOUNT) {
            String formatted = formatDiscountAmount(coupon.benefitValue());
            return "요금 " + formatted + "원 할인";
        }
        return coupon.benefitValue();
    }

    /**
     * benefitText: DISCOUNT → 숫자 포맷 + "원 할인", DATA → 문자열 그대로.
     */
    private static String toBenefitText(Coupon coupon) {
        if (coupon.couponType() == CouponType.DISCOUNT) {
            return formatDiscountAmount(coupon.benefitValue()) + "원 할인";
        }
        return coupon.benefitValue();
    }

    private static String formatDiscountAmount(String benefitValue) {
        if (benefitValue == null || benefitValue.isBlank()) {
            return "0";
        }
        try {
            long amount = Long.parseLong(benefitValue.trim().replace(",", ""));
            return String.format("%,d", amount);
        } catch (NumberFormatException e) {
            return benefitValue;
        }
    }

    private static LocalDate toExpiredDate(Instant expiredAt) {
        if (expiredAt == null) {
            return null;
        }
        return LocalDate.ofInstant(expiredAt, ZONE);
    }
}
