package site.holliverse.admin.query.dao;


import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static site.holliverse.admin.query.jooq.Tables.*;

/**
 ==========================
 * $NAME
 * 쿠폰 관련 쿼리문을 작성하는 DAO.
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-16
 * ========================== */

@RequiredArgsConstructor
public class AdminChurnCouponDao {

    private final DSLContext dsl;

    public boolean existsCouponById(Long couponId){

        return dsl.fetchExists(
                dsl.selectFrom(COUPON)
                        .where(COUPON.COUPON_ID.eq(couponId))
        );
    }

    public Optional<CouponRawData> findCouponById(Long couponId){
        return dsl.select(
                COUPON.COUPON_ID.as("couponId"),
                COUPON.VALID_DAYS.as("validDays"),
                COUPON.VALID_END_DATE.as("validEndDate")
        )
                .from(COUPON)
                .where(COUPON.COUPON_ID.eq(couponId))
                .fetchOptionalInto(CouponRawData.class);
    }

    public Optional<ChurnCouponMemberRawData> findMemberById(Long memberId){
        return dsl.select(
                MEMBER.MEMBER_ID.as("memberId"),
                MEMBER.ROLE.cast(String.class).as("role")
        )
                .from(MEMBER)
                .where(MEMBER.MEMBER_ID.eq(memberId))
                .fetchOptionalInto(ChurnCouponMemberRawData.class);
    }


    public boolean existsIssuedCouponWithinDays(Long memberId, Long couponId, int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);

        return dsl.fetchExists(
                dsl.selectFrom(MEMBER_COUPON)
                        .where(MEMBER_COUPON.MEMBER_ID.eq(memberId))
                        .and(MEMBER_COUPON.COUPON_ID.eq(couponId))
                        .and(MEMBER_COUPON.ISSUED_AT.ge(threshold))
        );

    }

    public void insertMemberCoupon(Long memberId, Long couponId, LocalDateTime issuedAt, LocalDateTime expiredAt){

        dsl.insertInto(MEMBER_COUPON)
                .set(MEMBER_COUPON.MEMBER_ID, memberId)
                .set(MEMBER_COUPON.COUPON_ID, couponId)
                .set(MEMBER_COUPON.IS_USED, false)
                .set(MEMBER_COUPON.ISSUED_AT, issuedAt)
                .set(MEMBER_COUPON.EXPIRED_AT, expiredAt)
                .execute();

    }

    public java.util.List<CouponSmsTargetRawData> findCouponSmsTargets(Collection<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return java.util.List.of();
        }

        return dsl.select(
                        MEMBER.MEMBER_ID.as("memberId"),
                        MEMBER.PHONE.as("encryptedPhone")
                )
                .from(MEMBER)
                .where(MEMBER.MEMBER_ID.in(memberIds))
                .fetchInto(CouponSmsTargetRawData.class);
    }


}
