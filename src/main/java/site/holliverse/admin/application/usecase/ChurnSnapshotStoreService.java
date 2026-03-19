package site.holliverse.admin.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record2;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.domain.model.churn.ChurnEvaluationResult;
import site.holliverse.admin.domain.model.churn.ChurnRiskGrade;
import site.holliverse.admin.domain.model.churn.ChurnScore;
import site.holliverse.admin.domain.model.churn.ChurnScoreCalculationResult;
import site.holliverse.admin.domain.policy.churn.ChurnRiskGradePolicy;
import site.holliverse.admin.query.jooq.enums.FeatureType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static site.holliverse.admin.query.jooq.Tables.CHURN_FEATURE_SCORE;
import static site.holliverse.admin.query.jooq.Tables.CHURN_SCORE_SNAPSHOT;
import static site.holliverse.admin.query.jooq.Tables.FEATURE_SNAPSHOT_STORE;

@Service
@Profile("admin")
@RequiredArgsConstructor
public class ChurnSnapshotStoreService {
    // JOOQ 생성 코드에 아직 없는 신규 컬럼은 동적 필드로 참조한다.
    private static final Field<Long> CHURN_REVISION_ID = DSL.field(DSL.name("revision_id"), Long.class);
    private static final Field<java.time.OffsetDateTime> CHURN_UPDATED_AT =
            DSL.field(DSL.name("updated_at"), java.time.OffsetDateTime.class);
    // 같은 회원 row를 update해도 새 커서가 발급되도록 시퀀스를 직접 사용한다.
    private static final Field<Long> NEXT_CHURN_REVISION_ID =
            DSL.field("nextval('churn_score_revision_seq')", Long.class);

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;
    private final ChurnRiskGradePolicy churnRiskGradePolicy;

    /**
     * 스냅샷 저장.
     */
    @Transactional
    public ChurnEvaluationResult store(
            Long memberId,
            LocalDate baseDate,
            ChurnRiskReason.Feature updatedFeature,
            List<ChurnRiskReason> riskReasons
    ) {
        // 상세 점수
        FeatureScores featureScores = readLatestFeatureScores(memberId);

        // 총점 계산
        ChurnScore churnScore = ChurnScore.fromRaw(featureScores.totalScore());
        int totalScore = churnScore.value();

        // 등급 계산
        ChurnRiskGrade riskGrade = churnRiskGradePolicy.classify(totalScore);

        // 위험 사유 직렬화
        JSONB riskReasonsJson = JSONB.valueOf(writeRiskReasons(mergeRiskReasons(
                memberId,
                baseDate,
                updatedFeature,
                riskReasons
        )));

        // 부모 업서트
        Long snapshotId = dsl.insertInto(CHURN_SCORE_SNAPSHOT)
                .set(CHURN_SCORE_SNAPSHOT.MEMBER_ID, memberId)
                .set(CHURN_SCORE_SNAPSHOT.CHURN_SCORE, totalScore)
                .set(CHURN_SCORE_SNAPSHOT.RISK_LEVEL, riskGrade.name())
                .set(CHURN_SCORE_SNAPSHOT.RISK_REASONS, riskReasonsJson)
                .set(CHURN_SCORE_SNAPSHOT.BASE_DATE, baseDate)
                .set(CHURN_REVISION_ID, NEXT_CHURN_REVISION_ID)
                .set(CHURN_UPDATED_AT, DSL.currentOffsetDateTime())
                .onConflict(CHURN_SCORE_SNAPSHOT.MEMBER_ID, CHURN_SCORE_SNAPSHOT.BASE_DATE)
                .doUpdate()
                .set(CHURN_SCORE_SNAPSHOT.CHURN_SCORE, totalScore)
                .set(CHURN_SCORE_SNAPSHOT.RISK_LEVEL, riskGrade.name())
                .set(CHURN_SCORE_SNAPSHOT.RISK_REASONS, riskReasonsJson)
                .set(CHURN_REVISION_ID, NEXT_CHURN_REVISION_ID)
                .set(CHURN_UPDATED_AT, DSL.currentOffsetDateTime())
                .returning(CHURN_SCORE_SNAPSHOT.SNAPSHOT_ID)
                .fetchOne(CHURN_SCORE_SNAPSHOT.SNAPSHOT_ID);

        // 자식 업서트
        dsl.insertInto(CHURN_FEATURE_SCORE)
                .set(CHURN_FEATURE_SCORE.SNAPSHOT_ID, snapshotId)
                .set(CHURN_FEATURE_SCORE.CHURN_BASE_SCORE, featureScores.baseScore())
                .set(CHURN_FEATURE_SCORE.CHURN_USAGE_SCORE, featureScores.usageScore())
                .set(CHURN_FEATURE_SCORE.CHURN_COUNSEL_SCORE, featureScores.counselScore())
                .set(CHURN_FEATURE_SCORE.CHURN_LOG_SCORE, featureScores.logScore())
                .onConflict(CHURN_FEATURE_SCORE.SNAPSHOT_ID)
                .doUpdate()
                .set(CHURN_FEATURE_SCORE.CHURN_BASE_SCORE, featureScores.baseScore())
                .set(CHURN_FEATURE_SCORE.CHURN_USAGE_SCORE, featureScores.usageScore())
                .set(CHURN_FEATURE_SCORE.CHURN_COUNSEL_SCORE, featureScores.counselScore())
                .set(CHURN_FEATURE_SCORE.CHURN_LOG_SCORE, featureScores.logScore())
                .execute();

        return new ChurnEvaluationResult(
                new ChurnScoreCalculationResult(churnScore, List.of()),
                riskGrade
        );
    }

    /**
     * 최신 점수 조회.
     */
    private FeatureScores readLatestFeatureScores(Long memberId) {
        Map<FeatureType, Integer> latestScores = new EnumMap<>(FeatureType.class);

        List<Record2<FeatureType, Integer>> records = dsl.select(
                        FEATURE_SNAPSHOT_STORE.FEATURE_TYPE,
                        FEATURE_SNAPSHOT_STORE.FEATURE_SCORE
                )
                .from(FEATURE_SNAPSHOT_STORE)
                .where(FEATURE_SNAPSHOT_STORE.MEMBER_ID.eq(memberId))
                .orderBy(FEATURE_SNAPSHOT_STORE.UPDATED_AT.desc())
                .fetch();

        for (Record2<FeatureType, Integer> record : records) {
            latestScores.putIfAbsent(
                    record.get(FEATURE_SNAPSHOT_STORE.FEATURE_TYPE),
                    record.get(FEATURE_SNAPSHOT_STORE.FEATURE_SCORE)
            );
        }

        return new FeatureScores(
                toShortScore(latestScores.get(FeatureType.CONTRACT_FEATURE)),
                toShortScore(latestScores.get(FeatureType.USAGE_FEATURE)),
                toShortScore(latestScores.get(FeatureType.DISSATISFACTION_FEATURE)),
                toShortScore(latestScores.get(FeatureType.MEMBER_ACTION_FEATURE))
        );
    }

    /**
     * 위험 사유 병합.
     */
    private List<ChurnRiskReason> mergeRiskReasons(
            Long memberId,
            LocalDate baseDate,
            ChurnRiskReason.Feature updatedFeature,
            List<ChurnRiskReason> riskReasons
    ) {
        List<ChurnRiskReason> merged = new ArrayList<>();
        readRiskReasons(memberId, baseDate).stream()
                .filter(reason -> reason.feature() != updatedFeature)
                .forEach(merged::add);
        merged.addAll(riskReasons);
        return merged;
    }

    /**
     * 기존 사유 조회.
     */
    private List<ChurnRiskReason> readRiskReasons(Long memberId, LocalDate baseDate) {
        JSONB riskReasonsJson = dsl.select(CHURN_SCORE_SNAPSHOT.RISK_REASONS)
                .from(CHURN_SCORE_SNAPSHOT)
                .where(CHURN_SCORE_SNAPSHOT.MEMBER_ID.eq(memberId))
                .and(CHURN_SCORE_SNAPSHOT.BASE_DATE.eq(baseDate))
                .fetchOptional(CHURN_SCORE_SNAPSHOT.RISK_REASONS)
                .orElse(null);

        if (riskReasonsJson == null || riskReasonsJson.data() == null || riskReasonsJson.data().isBlank()) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(riskReasonsJson.data());
            if (!root.isArray()) {
                return List.of();
            }

            List<ChurnRiskReason> reasons = new ArrayList<>();
            for (JsonNode item : root) {
                if (!item.isObject()) {
                    continue;
                }

                try {
                    reasons.add(objectMapper.convertValue(item, ChurnRiskReason.class));
                } catch (IllegalArgumentException ignored) {
                    // 구형 포맷 무시
                }
            }
            return reasons;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("risk reasons deserialization failed", e);
        }
    }

    /**
     * 위험 사유 직렬화.
     */
    private String writeRiskReasons(List<ChurnRiskReason> riskReasons) {
        try {
            return objectMapper.writeValueAsString(riskReasons);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("risk reasons serialization failed", e);
        }
    }

    /**
     * 점수 변환.
     */
    private Short toShortScore(Integer featureScore) {
        return featureScore == null ? 0 : featureScore.shortValue();
    }

    /**
     * 점수 묶음.
     */
    private record FeatureScores(
            Short baseScore,
            Short usageScore,
            Short counselScore,
            Short logScore
    ) {
        /**
         * 총점 합계.
         */
        private int totalScore() {
            return score(baseScore) + score(usageScore) + score(counselScore) + score(logScore);
        }

        /**
         * 점수 계산.
         */
        private int score(Short value) {
            return value == null ? 0 : value;
        }
    }
}
