package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Sequence;
import org.springframework.context.annotation.Profile;
import site.holliverse.admin.domain.model.churn.feature.MemberActionFeature;
import site.holliverse.admin.query.jooq.enums.FeatureType;

import java.util.Optional;

import static org.jooq.impl.DSL.currentLocalDateTime;
import static org.jooq.impl.DSL.sequence;
import static site.holliverse.admin.query.jooq.Tables.FEATURE_SNAPSHOT_STORE;
import static site.holliverse.admin.query.jooq.Tables.MEMBER_ACTION_FEATURE;

/**
 * 로그 feature 스냅샷 DAO.
 */
@Profile("admin")
@RequiredArgsConstructor
public class MemberActionFeatureLogDao {

    private static final Sequence<Long> FEATURE_SNAPSHOT_ID_SEQ =
            sequence("feature_snapshot_id_seq", Long.class);

    private final DSLContext dsl;

    /**
     * 최신 스냅샷 조회.
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
     * 스냅샷 조회.
     */
    public ActionSnapshot findSnapshot(Long snapshotId) {
        return dsl.select(
                        MEMBER_ACTION_FEATURE.FEATURE_SNAPSHOT_ID,
                        MEMBER_ACTION_FEATURE.CHANGE_MOBILE_CNT,
                        MEMBER_ACTION_FEATURE.COMPARISON_CNT,
                        MEMBER_ACTION_FEATURE.CHECKED_PENALTY_FEE_CNT
                )
                .from(MEMBER_ACTION_FEATURE)
                .where(MEMBER_ACTION_FEATURE.FEATURE_SNAPSHOT_ID.eq(snapshotId))
                .fetchOptional(record -> new ActionSnapshot(
                        record.get(MEMBER_ACTION_FEATURE.FEATURE_SNAPSHOT_ID),
                        new MemberActionFeature(
                                Optional.ofNullable(record.get(MEMBER_ACTION_FEATURE.CHANGE_MOBILE_CNT))
                                        .map(Short::intValue)
                                        .orElse(0),
                                Optional.ofNullable(record.get(MEMBER_ACTION_FEATURE.COMPARISON_CNT))
                                        .orElse(0),
                                Optional.ofNullable(record.get(MEMBER_ACTION_FEATURE.CHECKED_PENALTY_FEE_CNT))
                                        .orElse(0)
                        )
                ))
                .orElseThrow(() -> new IllegalStateException("member action snapshot not found. snapshotId=" + snapshotId));
    }

    /**
     * 스냅샷 조회 또는 생성.
     */
    public ActionSnapshot getOrCreateSnapshot(Long memberId) {
        long snapshotId = findLatestSnapshotId(memberId)
                .orElseGet(() -> createSnapshotForMemberActionFeature(memberId));
        return findSnapshot(snapshotId);
    }

    /**
     * 스냅샷 생성.
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

    /**
     * 스냅샷 동기화.
     */
    public void syncSnapshot(Long snapshotId, int featureScore, MemberActionFeature feature) {
        dsl.update(FEATURE_SNAPSHOT_STORE)
                .set(FEATURE_SNAPSHOT_STORE.UPDATED_AT, currentLocalDateTime())
                .set(FEATURE_SNAPSHOT_STORE.FEATURE_SCORE, featureScore)
                .where(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID.eq(snapshotId))
                .execute();

        dsl.update(MEMBER_ACTION_FEATURE)
                .set(MEMBER_ACTION_FEATURE.CHANGE_MOBILE_CNT, (short) feature.changeMobileCount())
                .set(MEMBER_ACTION_FEATURE.COMPARISON_CNT, feature.comparisonCount())
                .set(MEMBER_ACTION_FEATURE.CHECKED_PENALTY_FEE_CNT, feature.checkedPenaltyFeeCount())
                .where(MEMBER_ACTION_FEATURE.FEATURE_SNAPSHOT_ID.eq(snapshotId))
                .execute();
    }

    /**
     * 스냅샷 묶음.
     */
    public record ActionSnapshot(
            Long snapshotId,
            MemberActionFeature feature
    ) {
    }
}
