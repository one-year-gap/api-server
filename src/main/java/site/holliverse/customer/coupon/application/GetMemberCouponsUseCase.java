package site.holliverse.customer.coupon.application;

import site.holliverse.customer.coupon.domain.MemberCoupon;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 고객 보유(미사용·유효기간 내) 쿠폰 목록 조회 UseCase.
 */
@Service
@Profile("customer")
public class GetMemberCouponsUseCase {

    @Transactional(readOnly = true)
    public List<MemberCoupon> getAvailableCoupons(Long memberId) {
        // TODO: MemberCouponReader 연동 구현
        return List.of();
    }
}
