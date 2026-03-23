package site.holliverse.coupon.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.error.AdminErrorCode;
import site.holliverse.admin.error.AdminException;
import site.holliverse.customer.persistence.entity.Coupon;
import site.holliverse.customer.persistence.entity.MemberCoupon;
import site.holliverse.coupon.repository.CouponRepository;
import site.holliverse.coupon.repository.MemberCouponGrantRepository;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.time.LocalDateTime;

/**
 ========================== 
 * $NAME
 * 회원에게 쿠폰을 실제 발급하는 공통 서비스
 * 쿠폰 조회, 만료일 계산, member_coupon 저장을 담당
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-22
 * ========================== */

@Service
@RequiredArgsConstructor
public class CouponGrantService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final MemberCouponGrantRepository memberCouponGrantRepository;


    /**
     * 특정 회원에게 특정 쿠폰 1장을 발급
     * @param memberId
     * @param couponId
     */
    @Transactional
    public void grant(Long memberId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new AdminException(
                        AdminErrorCode.COUPON_NOT_FOUND,
                        "couponId"
                ));

        LocalDateTime now = LocalDateTime.now();
        Member member = memberRepository.getReferenceById(memberId);

        memberCouponGrantRepository.save(
                MemberCoupon.builder()
                        .member(member)
                        .coupon(coupon)
                        .issuedAt(now)
                        .expiredAt(resolveExpiredAt(coupon, now))
                        .build()
        );
    }

    /**
     * 쿠폰의 유효기간 정책에 따라 만료일 계산
     * 유효기간이 있으면 발급시점 기준으로 계산하고 없으면 endDate를 그대로 계산해서 사용
     * @param coupon
     * @param now
     * @return
     */
        private LocalDateTime resolveExpiredAt(Coupon coupon, LocalDateTime now) {
        if (coupon.getValidDays() != null) {
            return now.plusDays(coupon.getValidDays());
        }

        if (coupon.getValidEndDate() != null) {
            return coupon.getValidEndDate();
        }

        throw new AdminException(
                AdminErrorCode.COUPON_EXPIRATION_DATE_UNAVAILABLE,
                "couponId"
        );
    }
}