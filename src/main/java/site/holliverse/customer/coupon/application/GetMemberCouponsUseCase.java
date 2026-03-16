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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 고객 보유(미사용·유효기간 내) 쿠폰 목록 조회 UseCase.
 */
@Service
@Profile("customer")
public class GetMemberCouponsUseCase {

    private final MemberCouponRepository memberCouponRepository;

    public GetMemberCouponsUseCase(MemberCouponRepository memberCouponRepository) {
        this.memberCouponRepository = memberCouponRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberCoupon> getAvailableCoupons(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        List<site.holliverse.customer.persistence.entity.MemberCoupon> list =
                memberCouponRepository.findAvailableByMemberId(memberId, now);
        return list.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
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
