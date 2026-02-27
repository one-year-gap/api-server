package site.holliverse.admin.query.dao;


import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import site.holliverse.admin.query.jooq.enums.MemberMembershipType;
import site.holliverse.admin.query.jooq.enums.MemberRoleType;
import site.holliverse.admin.query.jooq.enums.MemberStatusType;
import site.holliverse.shared.domain.model.MemberRole;
import site.holliverse.shared.domain.model.MemberStatus;

import static org.jooq.impl.DSL.count;
import static site.holliverse.admin.query.jooq.Tables.MEMBER;

@Profile("admin")

@RequiredArgsConstructor
public class AdminMembershipStatDao {

    private final DSLContext dsl;

    /**
     * 멤버 전체수, vvipcount, vipcount , goldCount 구하기
     *
     */

    public AdminMembershipStatRawData getMembershipStats() {
        AdminMembershipStatRawData raw = dsl.select(
                        count().as("totalCount"),
                        count().filterWhere(MEMBER.MEMBERSHIP.eq(MemberMembershipType.VVIP)).as("vvipCount"),
                        count().filterWhere(MEMBER.MEMBERSHIP.eq(MemberMembershipType.VIP)).as("vipCount"),
                        count().filterWhere(MEMBER.MEMBERSHIP.eq(MemberMembershipType.GOLD)).as("goldCount")
                )
                .from(MEMBER)
                .where(MEMBER.ROLE.eq(MemberRoleType.CUSTOMER))
                .and(MEMBER.STATUS.eq(MemberStatusType.ACTIVE))
                .fetchOneInto(AdminMembershipStatRawData.class);

        return raw !=null? raw:new AdminMembershipStatRawData(0L,0L,0L,0L);
    }

}
