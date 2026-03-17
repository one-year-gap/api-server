package site.holliverse.admin.query.dao;

public record CouponSmsTargetRawData(
        Long memberId,
        String encryptedPhone
) {
}
