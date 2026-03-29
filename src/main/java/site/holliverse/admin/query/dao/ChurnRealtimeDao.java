package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import site.holliverse.admin.query.jooq.enums.MemberRoleType;
import site.holliverse.admin.web.dto.churn.ChurnRealTimeRequestDto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static site.holliverse.admin.query.jooq.Tables.CHURN_SCORE_SNAPSHOT;
import static site.holliverse.admin.query.jooq.Tables.MEMBER;

@Profile("admin")
@Repository
@RequiredArgsConstructor
public class ChurnRealtimeDao {
    private static final Field<Long> REVISION_ID = DSL.field(DSL.name("revision_id"), Long.class);
    private static final Field<OffsetDateTime> UPDATED_AT = DSL.field(DSL.name("updated_at"), OffsetDateTime.class);

    private final DSLContext dsl;

    /**
     * 현재 시점의 최신 스냅샷 목록 조회
     */
    public List<ChurnRealtimeRawData> findLatest(ChurnRealTimeRequestDto requestDto) {
        return selectBase(requestDto)
                .orderBy(REVISION_ID.desc(), CHURN_SCORE_SNAPSHOT.SNAPSHOT_ID.desc())
                .limit(requestDto.normalizedLimit() + 1)
                .fetchInto(ChurnRealtimeRawData.class);
    }

    /**
     * 마지막 커서 이후 변경분만 조회
     */
    public List<ChurnRealtimeRawData> findChanges(ChurnRealTimeRequestDto requestDto) {
        SelectConditionStep<?> query = selectBase(requestDto);
        if (requestDto.normalizedAfterId() > 0) {
            query = query.and(REVISION_ID.gt(requestDto.normalizedAfterId()));
        }

        return query
                .orderBy(REVISION_ID.asc(), CHURN_SCORE_SNAPSHOT.SNAPSHOT_ID.asc())
                .limit(requestDto.normalizedLimit() + 1)
                .fetchInto(ChurnRealtimeRawData.class);
    }

    /**
     * latest/changes가 공통으로 쓰는 기본 조회
     */
    private SelectConditionStep<?> selectBase(ChurnRealTimeRequestDto requestDto) {
        return dsl.select(
                        REVISION_ID.as("churnId"),
                        MEMBER.MEMBER_ID.as("memberId"),
                        MEMBER.NAME.as("encryptedName"),
                        CHURN_SCORE_SNAPSHOT.RISK_LEVEL.as("churnLevel"),
                        CHURN_SCORE_SNAPSHOT.RISK_REASONS.cast(String.class).as("riskReasons"),
                        UPDATED_AT.as("timeStamp")
                )
                .from(CHURN_SCORE_SNAPSHOT)
                .join(MEMBER).on(CHURN_SCORE_SNAPSHOT.MEMBER_ID.eq(MEMBER.MEMBER_ID))
                .where(createConditions(requestDto));
    }

    /**
     * 오늘 기준 CUSTOMER 스냅샷.
     */
    private List<Condition> createConditions(ChurnRealTimeRequestDto requestDto) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(CHURN_SCORE_SNAPSHOT.BASE_DATE.eq(LocalDate.now()));
        conditions.add(MEMBER.ROLE.eq(MemberRoleType.CUSTOMER));
        conditions.add(CHURN_SCORE_SNAPSHOT.RISK_LEVEL.in(requestDto.riskLevels()));
        return conditions;
    }
}
