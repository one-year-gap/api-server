package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.AdminChurnCouponDao;
import site.holliverse.admin.query.dao.ChurnCouponMemberRawData;
import site.holliverse.coupon.application.CouponGrantService;


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
    private final CouponGrantService couponGrantService;

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

        couponGrantService.grant(memberId, couponId);
        return IssueOneChurnCouponResult.issued(memberId);
    }
}