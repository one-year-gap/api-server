package site.holliverse.admin.query.dao;

import java.time.LocalDateTime;


/**
 ==========================
 * $NAME
 * 쿠폰아이디 ,90일 , 엔드기간
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-16
 * ========================== */
public record CouponRawData(
        Long couponId,
        Integer validDays,
        LocalDateTime validEndDate
) {
}
