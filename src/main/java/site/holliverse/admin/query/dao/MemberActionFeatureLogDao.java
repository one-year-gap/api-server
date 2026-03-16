package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.jooq.DSLContext;
import org.jooq.Sequence;
import org.jooq.impl.DSL;

import java.util.Optional;

import site.holliverse.admin.query.jooq.enums.FeatureType;

import static org.jooq.impl.DSL.currentLocalDateTime;
import static org.jooq.impl.DSL.sequence;
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

    private static final Sequence<Long> FEATURE_SNAPSHOT_ID_SEQ =
            sequence("feature_snapshot_id_seq", Long.class);

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

    /**
     * 스냅샷 조회/생성 정책: 최신 스냅샷이 있으면 그 ID, 없으면 1건 생성 후 해당 ID 반환.
     * (최신 = member_id + MEMBER_ACTION_FEATURE, updated_at 내림차순 1건)
     */
    public long getOrCreateSnapshotId(Long memberId) {
        return findLatestSnapshotId(memberId)
                .orElseGet(() -> createSnapshotForMemberActionFeature(memberId));
    }

    /**
     * 해당 회원의 MEMBER_ACTION_FEATURE 스냅샷이 없을 때 1건 생성.
     * feature_snapshot_id는 feature_snapshot_id_seq 시퀀스로 발급.
     * member 테이블에 해당 member_id가 있어야 FK 제약으로 정상 동작.
     *
     * @return 새로 생성된 feature_snapshot_id
     */
    public long createSnapshotForMemberActionFeature(Long memberId) {
        Long snapshotId = dsl
                .insertInto(FEATURE_SNAPSHOT_STORE)
                .set(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID, dsl.nextval(FEATURE_SNAPSHOT_ID_SEQ))
                .set(FEATURE_SNAPSHOT_STORE.MEMBER_ID, memberId)
                .set(FEATURE_SNAPSHOT_STORE.FEATURE_TYPE, FeatureType.MEMBER_ACTION_FEATURE)
                .set(FEATURE_SNAPSHOT_STORE.CREATED_AT, currentLocalDateTime())
                .set(FEATURE_SNAPSHOT_STORE.UPDATED_AT, currentLocalDateTime())
                .set(FEATURE_SNAPSHOT_STORE.FEATURE_SCORE, 0)
                .returning(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID)
                .fetchOne()
                .get(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID);

        dsl.insertInto(MEMBER_ACTION_FEATURE)
                .set(MEMBER_ACTION_FEATURE.FEATURE_SNAPSHOT_ID, snapshotId)
                .set(MEMBER_ACTION_FEATURE.CHANGE_MOBILE_CNT, (short) 0)
                .set(MEMBER_ACTION_FEATURE.COMPARISON_CNT, 0)
                .set(MEMBER_ACTION_FEATURE.CHECKED_PENALTY_FEE_CNT, 0)
                .execute();
        return snapshotId.longValue();
    }
}
