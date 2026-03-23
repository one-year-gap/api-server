package site.holliverse.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Coupon;

/**
 ==========================
 * $NAME
 * 쿠폰 발급에 필요한 coupon 엔티티 조회를 담당하는 저장소.
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-22
 * ========================== */
public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
