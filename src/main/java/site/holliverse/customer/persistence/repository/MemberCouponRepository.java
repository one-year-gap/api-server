package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.holliverse.customer.persistence.entity.MemberCoupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 회원 보유 쿠폰(member_coupon) JPA 저장소.
 * 미사용·유효기간 내 쿠폰 목록 조회 시 사용.
 */
@Profile("customer")
public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    /**
     * 회원별 미사용·유효기간 내 쿠폰 목록 (만료일 오름차순).
     * coupon JOIN FETCH로 N+1 방지.
     */
    @Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon " +
           "WHERE mc.member.id = :memberId AND mc.isUsed = false AND mc.expiredAt > :now " +
           "ORDER BY mc.expiredAt ASC")
    List<MemberCoupon> findAvailableByMemberId(
            @Param("memberId") Long memberId,
            @Param("now") LocalDateTime now);

    /** 회원 소유 쿠폰 1건 조회 (쿠폰 정보 JOIN FETCH). member는 lazy 로드. */
    @Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon WHERE mc.id = :memberCouponId AND mc.member.id = :memberId")
    Optional<MemberCoupon> findByIdAndMemberId(
            @Param("memberCouponId") Long memberCouponId,
            @Param("memberId") Long memberId);

    /** 회원별 미사용·유효기간 내 쿠폰 개수. */
    long countByMember_IdAndIsUsedFalseAndExpiredAtAfter(Long memberId, LocalDateTime now);
}
