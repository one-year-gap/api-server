package site.holliverse.admin.application.usecase;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.AdminChurnCouponDao;
import site.holliverse.admin.query.dao.ChurnCouponMemberRawData;
import site.holliverse.admin.query.dao.CouponRawData;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.time.LocalDateTime;

/**
 ==========================
 * $NAME
 * 회원 1명 조회
 * 그 회원이 CUSTOMER 인지 확인하고  최근 90일 내 같은 쿠폰 발급 이력이 있는지 확인한다
 * 쿠폰 정보를 조회 한다음에 만료일 계산한다 .
 * 그리고 insert
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-16
 * ========================== */

@Profile("admin")
@Service
@RequiredArgsConstructor
public class ChurnCouponIssueProcessor {

    private final AdminChurnCouponDao adminChurnCouponDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IssueOneChurnCouponResult issue(Long memberId, Long couponId) {
        ChurnCouponMemberRawData member = adminChurnCouponDao.findMemberById(memberId)
                .orElse(null);

        if (member == null) {
            return IssueOneChurnCouponResult.skipped(memberId, "MEMBER_NOT_FOUND");
        }

        if (!"CUSTOMER".equals(member.role())) {
            return IssueOneChurnCouponResult.skipped(memberId, "NOT_CUSTOMER");
        }

        boolean alreadyIssued = adminChurnCouponDao.existsIssuedCouponWithinDays(memberId, couponId, 90);
        if (alreadyIssued) {
            return IssueOneChurnCouponResult.skipped(memberId, "ALREADY_ISSUED_WITHIN_90_DAYS");
        }

        CouponRawData coupon = adminChurnCouponDao.findCouponById(couponId)
                .orElse(null);

        if (coupon == null) {
            return IssueOneChurnCouponResult.skipped(memberId, "COUPON_NOT_FOUND");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = resolveExpiredAt(coupon, now);

        adminChurnCouponDao.insertMemberCoupon(memberId, couponId, now, expiredAt);

        return IssueOneChurnCouponResult.issued(memberId);
    }

    private LocalDateTime resolveExpiredAt(CouponRawData coupon, LocalDateTime now) {
        if (coupon.validDays() != null) {
            return now.plusDays(coupon.validDays());
        }

        if (coupon.validEndDate() != null) {
            return coupon.validEndDate();
        }

        throw new CustomException(
                ErrorCode.INTERNAL_ERROR,
                "couponId",
                "쿠폰 만료일 정보를 확인할 수 없습니다."
        );
    }
}

