package site.holliverse.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.MemberCoupon;

/**
 ==========================
 * $NAME
 * 쿠폰 발급시 member_coupon 저장 및 발급 이력 조회를 담당하는 레포지토리
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-22
 * ========================== */
public interface MemberCouponGrantRepository extends JpaRepository<MemberCoupon, Long> {

    // 특정 회원에게 특정 쿠폰이 이미 발급된적이 있는지 확인
    boolean existsByMember_IdAndCoupon_Id(Long memberId, Long couponId);
}