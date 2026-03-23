package site.holliverse.customer.coupon.application;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.coupon.domain.Coupon;
import site.holliverse.customer.coupon.domain.MemberCoupon;
import site.holliverse.customer.coupon.domain.MemberCouponStatus;
import site.holliverse.customer.error.CustomerErrorCode;
import site.holliverse.customer.error.CustomerException;
import site.holliverse.customer.persistence.repository.MemberCouponRepository;
import site.holliverse.infra.error.InfraErrorCode;
import site.holliverse.infra.error.InfraException;

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
                .orElseThrow(() -> new CustomerException(CustomerErrorCode.COUPON_NOT_FOUND));

        if (entity.getIsUsed()) {
            throw new CustomerException(CustomerErrorCode.COUPON_ALREADY_USED);
        }
        if (!entity.getExpiredAt().isAfter(now)) {
            throw new CustomerException(CustomerErrorCode.COUPON_EXPIRED);
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
            throw new InfraException(InfraErrorCode.COUPON_LOAD_FAILED);
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
