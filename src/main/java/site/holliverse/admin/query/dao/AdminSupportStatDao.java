package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import site.holliverse.admin.query.jooq.enums.SupportStatus;
import static site.holliverse.admin.query.jooq.Tables.SUPPORT_CASE;
import static org.jooq.impl.DSL.count;

/**
 * 전체 상담 처리 현황 통계를 조회하는 DAO
 */
@Component
@RequiredArgsConstructor
public class AdminSupportStatDao {

    private final DSLContext dsl;

    public AdminSupportStatRawData getSupportStatusStats() {
        return dsl.select(
                        // 1. 총 상담 건수
                        count().as("totalCount"),

                        // 2. 미처리(OPEN) 건수
                        count().filterWhere(SUPPORT_CASE.STATUS.eq(SupportStatus.OPEN)).as("openCount"),

                        // 3. 진행중(SUPPORTING) 건수
                        count().filterWhere(SUPPORT_CASE.STATUS.eq(SupportStatus.SUPPORTING)).as("supportingCount"),

                        // 4. 완료(CLOSED) 건수
                        count().filterWhere(SUPPORT_CASE.STATUS.eq(SupportStatus.CLOSED)).as("closedCount")
                )
                .from(SUPPORT_CASE)
                .fetchOneInto(AdminSupportStatRawData.class);
    }
}