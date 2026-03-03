package site.holliverse.admin.query.dao;

public record AdminMembershipStatRawData(
        Long totalCount,
        Long vvipCount,
        Long vipCount,
        Long goldCount
) {
}
