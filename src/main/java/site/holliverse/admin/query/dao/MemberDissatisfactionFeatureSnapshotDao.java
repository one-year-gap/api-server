package site.holliverse.admin.query.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record4;
import org.jooq.Sequence;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import site.holliverse.admin.query.jooq.enums.FeatureType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.jooq.impl.DSL.currentLocalDateTime;
import static org.jooq.impl.DSL.sequence;
import static site.holliverse.admin.query.jooq.Tables.FEATURE_SNAPSHOT_STORE;
import static site.holliverse.admin.query.jooq.Tables.MEMBER_DISSATISFACTION_FEATURE;

@Repository
@Profile("admin")
@RequiredArgsConstructor
public class MemberDissatisfactionFeatureSnapshotDao {

    private static final Sequence<Long> FEATURE_SNAPSHOT_ID_SEQ =
            sequence("feature_snapshot_id_seq", Long.class);

    private static final TypeReference<Map<String, Integer>> KEYWORD_COUNT_TYPE = new TypeReference<>() {
    };

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    /**
     * 스냅샷 조회.
     */
    public Optional<Long> findLatestSnapshotId(Long memberId) {
        return dsl.select(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID)
                .from(FEATURE_SNAPSHOT_STORE)
                .where(FEATURE_SNAPSHOT_STORE.MEMBER_ID.eq(memberId))
                .and(FEATURE_SNAPSHOT_STORE.FEATURE_TYPE.eq(FeatureType.DISSATISFACTION_FEATURE))
                .orderBy(FEATURE_SNAPSHOT_STORE.UPDATED_AT.desc())
                .limit(1)
                .fetchOptional(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID);
    }

    /**
     * 스냅샷 생성.
     */
    public long createSnapshot(Long memberId) {
        Long snapshotId = dsl.insertInto(FEATURE_SNAPSHOT_STORE)
                .set(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID, dsl.nextval(FEATURE_SNAPSHOT_ID_SEQ))
                .set(FEATURE_SNAPSHOT_STORE.MEMBER_ID, memberId)
                .set(FEATURE_SNAPSHOT_STORE.FEATURE_TYPE, FeatureType.DISSATISFACTION_FEATURE)
                .set(FEATURE_SNAPSHOT_STORE.CREATED_AT, currentLocalDateTime())
                .set(FEATURE_SNAPSHOT_STORE.UPDATED_AT, currentLocalDateTime())
                .set(FEATURE_SNAPSHOT_STORE.FEATURE_SCORE, 0)
                .returning(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID)
                .fetchOne(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID);

        dsl.insertInto(MEMBER_DISSATISFACTION_FEATURE)
                .set(MEMBER_DISSATISFACTION_FEATURE.FEATURE_SNAPSHOT_ID, snapshotId)
                .set(MEMBER_DISSATISFACTION_FEATURE.STAR_MEAN_SCORE, BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP))
                .set(MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_CNT, 0)
                .set(MEMBER_DISSATISFACTION_FEATURE.TERMINATING_KEYWORD_CNT, JSONB.valueOf("{}"))
                .set(MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_COUNSEL_CNT, 0)
                .set(MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_KEYWORD_CNT, 0)
                .execute();

        return snapshotId;
    }

    /**
     * 스냅샷 조회 또는 생성.
     */
    public long getOrCreateSnapshotId(Long memberId) {
        return findLatestSnapshotId(memberId)
                .orElseGet(() -> createSnapshot(memberId));
    }

    /**
     * 스냅샷 갱신.
     */
    public void syncSnapshot(
            Long snapshotId,
            int featureScore,
            double starMeanScore,
            int negativeCounselIncrement,
            Map<String, Integer> negativeKeywordCounts
    ) {
        Record4<Integer, Integer, Integer, JSONB> current = dsl.select(
                        MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_CNT,
                        MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_COUNSEL_CNT,
                        MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_KEYWORD_CNT,
                        MEMBER_DISSATISFACTION_FEATURE.TERMINATING_KEYWORD_CNT
                )
                .from(MEMBER_DISSATISFACTION_FEATURE)
                .where(MEMBER_DISSATISFACTION_FEATURE.FEATURE_SNAPSHOT_ID.eq(snapshotId))
                .fetchOne();

        if (current == null) {
            throw new IllegalStateException("member dissatisfaction snapshot not found. snapshotId=" + snapshotId);
        }

        int negativeKeywordIncrement = negativeKeywordCounts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        Map<String, Integer> mergedKeywordCounts = mergeKeywordCounts(
                readKeywordCounts(current.get(MEMBER_DISSATISFACTION_FEATURE.TERMINATING_KEYWORD_CNT)),
                negativeKeywordCounts
        );

        dsl.update(FEATURE_SNAPSHOT_STORE)
                .set(FEATURE_SNAPSHOT_STORE.UPDATED_AT, currentLocalDateTime())
                .set(FEATURE_SNAPSHOT_STORE.FEATURE_SCORE, featureScore)
                .where(FEATURE_SNAPSHOT_STORE.FEATURE_SNAPSHOT_ID.eq(snapshotId))
                .execute();

        dsl.update(MEMBER_DISSATISFACTION_FEATURE)
                .set(MEMBER_DISSATISFACTION_FEATURE.STAR_MEAN_SCORE, toScore(starMeanScore))
                .set(MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_CNT, current.get(MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_CNT) + negativeCounselIncrement)
                .set(MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_COUNSEL_CNT, current.get(MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_COUNSEL_CNT) + negativeCounselIncrement)
                .set(MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_KEYWORD_CNT, current.get(MEMBER_DISSATISFACTION_FEATURE.NEGATIVE_KEYWORD_CNT) + negativeKeywordIncrement)
                .set(MEMBER_DISSATISFACTION_FEATURE.TERMINATING_KEYWORD_CNT, JSONB.valueOf(writeKeywordCounts(mergedKeywordCounts)))
                .where(MEMBER_DISSATISFACTION_FEATURE.FEATURE_SNAPSHOT_ID.eq(snapshotId))
                .execute();
    }

    /**
     * 점수 스케일.
     */
    private BigDecimal toScore(double starMeanScore) {
        return BigDecimal.valueOf(starMeanScore)
                .setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 키워드 병합.
     */
    private Map<String, Integer> mergeKeywordCounts(
            Map<String, Integer> currentKeywordCounts,
            Map<String, Integer> negativeKeywordCounts
    ) {
        Map<String, Integer> merged = new LinkedHashMap<>(currentKeywordCounts);
        negativeKeywordCounts.forEach((keyword, count) -> merged.merge(keyword, count, Integer::sum));
        return merged;
    }

    /**
     * 키워드 역직렬화.
     */
    private Map<String, Integer> readKeywordCounts(JSONB keywordCountsJson) {
        if (keywordCountsJson == null || keywordCountsJson.data() == null || keywordCountsJson.data().isBlank()) {
            return new LinkedHashMap<>();
        }

        try {
            return objectMapper.readValue(keywordCountsJson.data(), KEYWORD_COUNT_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("terminating keyword counts deserialization failed", e);
        }
    }

    /**
     * 키워드 직렬화.
     */
    private String writeKeywordCounts(Map<String, Integer> keywordCounts) {
        try {
            return objectMapper.writeValueAsString(keywordCounts);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("terminating keyword counts serialization failed", e);
        }
    }
}
