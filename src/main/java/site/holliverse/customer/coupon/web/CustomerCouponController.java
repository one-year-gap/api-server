package site.holliverse.customer.coupon.web;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.customer.coupon.application.GetMemberCouponsUseCase;
import site.holliverse.customer.coupon.application.UseMemberCouponResult;
import site.holliverse.customer.coupon.application.UseMemberCouponUseCase;
import site.holliverse.customer.coupon.domain.MemberCoupon;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.coupon.CustomerCouponListResponse;
import site.holliverse.customer.web.dto.coupon.UseMemberCouponResponse;
import site.holliverse.shared.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.List;

/**
     * 고객 쿠폰 조회·사용 API.
     * - GET /api/v1/customer/coupons : 보유(미사용·유효기간 내) 쿠폰 목록 조회
     * - POST /api/v1/customer/coupons/use?memberCouponId=... : 쿠폰 사용 처리
 */
@RestController
@RequestMapping("/api/v1/customer/coupons")
@Profile("customer")
@RequiredArgsConstructor
public class CustomerCouponController {

    private final GetMemberCouponsUseCase getMemberCouponsUseCase;
    private final UseMemberCouponUseCase useMemberCouponUseCase;
    private final CustomerCouponMapper customerCouponMapper;

    @GetMapping
    public ApiResponse<CustomerCouponListResponse> getCoupons(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        Long memberId = customUserDetails.getMemberId();
        List<MemberCoupon> coupons = getMemberCouponsUseCase.getAvailableCoupons(memberId);
        CustomerCouponListResponse response = customerCouponMapper.toListResponse(coupons);
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }

    @PostMapping("/use")
    public ApiResponse<UseMemberCouponResponse> useCoupon(
            @RequestParam Long memberCouponId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        Long memberId = customUserDetails.getMemberId();
        UseMemberCouponResult result = useMemberCouponUseCase.useCoupon(memberId, memberCouponId);
        UseMemberCouponResponse response = customerCouponMapper.toUseResponse(result);
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }
}
