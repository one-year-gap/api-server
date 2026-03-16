package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.util.Optional;

import site.holliverse.admin.query.jooq.enums.FeatureType;
import site.holliverse.admin.query.jooq.tables.records.FeatureSnapshotStoreRecord;

import static site.holliverse.admin.query.jooq.Tables.FEATURE_SNAPSHOT_STORE;
import static site.holliverse.admin.query.jooq.Tables.MEMBER_ACTION_FEATURE;

/**
 * member_action_feature의 comparison_cnt / checked_penalty_fee_cnt 실시간 증분 갱신용 DAO.
 * feature_snapshot_store에서 해당 회원·MEMBER_ACTION_FEATURE 최신 스냅샷 조회 후 갱신.
 * 스냅샷 없을 때 생성 정책: 시퀀스로 새 ID 발급 후 feature_snapshot_store + member_action_feature 1건씩 INSERT.
 */
@Profile("admin")
@RequiredArgsConstructor
public class MemberActionFeatureLogDao {

    private static final String FEATURE_SNAPSHOT_ID_SEQ = "feature_snapshot_id_seq";

    private final DSLContext dsl;

    /**
     * member_id + MEMBER_ACTION_FEATURE 기준 최신 스냅샷 ID 조회 (updated_at 내림차순 1건).
     */
    public Optional<Long> findLatestSnapshotId(Long memberId) {
        return dsl
                .select(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID)
                .from(FEATURE_SNAPSHOT_STORE)
                .where(FEATURE_SNAPSHOT_STORE.MEMBER_ID.eq(memberId))
                .and(FEATURE_SNAPSHOT_STORE.FEATURE_TYPE.eq(FeatureType.MEMBER_ACTION_FEATURE))
                .orderBy(FEATURE_SNAPSHOT_STORE.UPDATED_AT.desc())
                .limit(1)
                .fetchOptional(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID);
    }

    /**
     * member_action_feature의 comparison_cnt, checked_penalty_fee_cnt에 증분 반영.
     */
    public int incrementCounts(Long featureSnapshotId, int comparisonIncrement, int penaltyIncrement) {
        if (comparisonIncrement == 0 && penaltyIncrement == 0) {
            return 0;
        }
        var ma = MEMBER_ACTION_FEATURE;
        var update = dsl.update(ma)
                .set(ma.COMPARISON_CNT, ma.COMPARISON_CNT.plus(comparisonIncrement))
                .set(ma.CHECKED_PENALTY_FEE_CNT, ma.CHECKED_PENALTY_FEE_CNT.plus(penaltyIncrement))
                .where(ma.FEATURE_SNAPSHOT_ID.eq(featureSnapshotId));
        return update.execute();
    }
}
