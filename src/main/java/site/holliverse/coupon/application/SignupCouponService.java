package site.holliverse.coupon.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.holliverse.coupon.repository.MemberCouponGrantRepository;

/**==========================
 * $NAME
 * 회원가입 축하 쿠폰 발급 정책을 담당하는 서비스.
 * 동일 회원에게 웰컴 쿠폰이 중복 발급되지 않도록 제어
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-22
 * ========================== */

@Service
@RequiredArgsConstructor
public class SignupCouponService {

    private static final Long WELCOME_COUPON_ID = 3L;

    private final CouponGrantService couponGrantService;
    private final MemberCouponGrantRepository memberCouponGrantRepository;

    /**
     * 회원 가입이 완료된 회원에게 웰컴 쿠폰 발급
     * 이미 발급된 경우에는 추가 발급하지 않는다.
     * @param memberId
     */
    public void issueWelcomeCoupon(Long memberId) {
        boolean alreadyIssued = memberCouponGrantRepository
                .existsByMember_IdAndCoupon_Id(memberId, WELCOME_COUPON_ID);
        if (alreadyIssued) {
            return;
        }

        couponGrantService.grant(memberId, WELCOME_COUPON_ID);
    }
}