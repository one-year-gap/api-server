package site.holliverse.customer.coupon.application;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.coupon.domain.Coupon;
import site.holliverse.customer.coupon.domain.MemberCoupon;
import site.holliverse.customer.coupon.domain.MemberCouponStatus;
import site.holliverse.customer.persistence.repository.MemberCouponRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 고객 쿠폰 사용 처리 UseCase.
 */
@Service
@Profile("customer")
public class UseMemberCouponUseCase {

    private final MemberCouponRepository memberCouponRepository;

    public UseMemberCouponUseCase(MemberCouponRepository memberCouponRepository) {
        this.memberCouponRepository = memberCouponRepository;
    }

    @Transactional
    public UseMemberCouponResult useCoupon(Long memberId, Long memberCouponId) {
        LocalDateTime now = LocalDateTime.now();

        site.holliverse.customer.persistence.entity.MemberCoupon entity = memberCouponRepository
                .findByIdAndMemberId(memberCouponId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND, "memberCouponId", "보유 쿠폰을 찾을 수 없습니다."));

        if (entity.getIsUsed()) {
            throw new CustomException(ErrorCode.COUPON_ALREADY_USED, "memberCouponId", "이미 사용된 쿠폰입니다.");
        }
        if (!entity.getExpiredAt().isAfter(now)) {
            throw new CustomException(ErrorCode.COUPON_EXPIRED, "memberCouponId", "만료된 쿠폰입니다.");
        }

        entity.markAsUsed(now);
        memberCouponRepository.save(entity);

        long remaining = memberCouponRepository.countByMember_IdAndIsUsedFalseAndExpiredAtAfter(memberId, now);
        MemberCoupon domainMemberCoupon = toDomain(entity);
        return new UseMemberCouponResult(domainMemberCoupon, (int) remaining);
    }

    private MemberCoupon toDomain(site.holliverse.customer.persistence.entity.MemberCoupon entity) {
        site.holliverse.customer.persistence.entity.Coupon c = entity.getCoupon();
        if (c == null) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "coupon", "쿠폰 정보를 불러오는 중 오류가 발생했습니다.");
        }
        Coupon domainCoupon = new Coupon(
                c.getId(),
                c.getName(),
                c.getCouponType(),
                c.getBenefitValue(),
                c.getDescription()
        );
        return new MemberCoupon(
                entity.getId(),
                entity.getMember().getId(),
                entity.getCoupon().getId(),
                entity.getIsUsed() ? MemberCouponStatus.USED : MemberCouponStatus.ISSUED,
                toInstant(entity.getIssuedAt()),
                entity.getUsedAt() != null ? toInstant(entity.getUsedAt()) : null,
                toInstant(entity.getExpiredAt()),
                domainCoupon
        );
    }

    private static java.time.Instant toInstant(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }
}
