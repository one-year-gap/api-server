package site.holliverse.admin.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.domain.model.churn.ChurnFeatureContribution;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.ChurnRiskGrade;
import site.holliverse.admin.domain.model.churn.ChurnScoreCalculationResult;

import java.time.LocalDate;
import java.util.List;

import static site.holliverse.admin.query.jooq.Tables.CHURN_FEATURE_SCORE;
import static site.holliverse.admin.query.jooq.Tables.CHURN_SCORE_SNAPSHOT;

@Service
@Profile("admin")
@RequiredArgsConstructor
public class ChurnSnapshotStoreService {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    @Transactional
    public void store(
            Long memberId,
            LocalDate baseDate,
            ChurnScoreCalculationResult result,
            ChurnRiskGrade grade
    ) {
        // 위험 사유 직렬화
        String riskReasons = writeRiskReasons(result.contributions());

        // 부모 업서트
        Long snapshotId = dsl.insertInto(CHURN_SCORE_SNAPSHOT)
                .set(CHURN_SCORE_SNAPSHOT.MEMBER_ID, memberId)
                .set(CHURN_SCORE_SNAPSHOT.CHURN_SCORE, result.score().value())
                .set(CHURN_SCORE_SNAPSHOT.RISK_LEVEL, grade.name())
                .set(CHURN_SCORE_SNAPSHOT.RISK_REASONS, JSONB.valueOf(riskReasons))
                .set(CHURN_SCORE_SNAPSHOT.BASE_DATE, baseDate)
                .onConflict(CHURN_SCORE_SNAPSHOT.MEMBER_ID, CHURN_SCORE_SNAPSHOT.BASE_DATE)
                .doUpdate()
                .set(CHURN_SCORE_SNAPSHOT.CHURN_SCORE, result.score().value())
                .set(CHURN_SCORE_SNAPSHOT.RISK_LEVEL, grade.name())
                .set(CHURN_SCORE_SNAPSHOT.RISK_REASONS, JSONB.valueOf(riskReasons))
                .returning(CHURN_SCORE_SNAPSHOT.SNAPSHOT_ID)
                .fetchOne(CHURN_SCORE_SNAPSHOT.SNAPSHOT_ID);

        // 자식 업서트
        dsl.insertInto(CHURN_FEATURE_SCORE)
                .set(CHURN_FEATURE_SCORE.SNAPSHOT_ID, snapshotId)
                .set(CHURN_FEATURE_SCORE.CHURN_BASE_SCORE, toShortScore(result, ChurnFeatureType.CONTRACT))
                .set(CHURN_FEATURE_SCORE.CHURN_USAGE_SCORE, toShortScore(result, ChurnFeatureType.USAGE))
                .set(CHURN_FEATURE_SCORE.CHURN_COUNSEL_SCORE, toShortScore(result, ChurnFeatureType.MEMBER_DISSATISFACTION))
                .set(CHURN_FEATURE_SCORE.CHURN_LOG_SCORE, toShortScore(result, ChurnFeatureType.MEMBER_ACTION))
                .onConflict(CHURN_FEATURE_SCORE.SNAPSHOT_ID)
                .doUpdate()
                .set(CHURN_FEATURE_SCORE.CHURN_BASE_SCORE, toShortScore(result, ChurnFeatureType.CONTRACT))
                .set(CHURN_FEATURE_SCORE.CHURN_USAGE_SCORE, toShortScore(result, ChurnFeatureType.USAGE))
                .set(CHURN_FEATURE_SCORE.CHURN_COUNSEL_SCORE, toShortScore(result, ChurnFeatureType.MEMBER_DISSATISFACTION))
                .set(CHURN_FEATURE_SCORE.CHURN_LOG_SCORE, toShortScore(result, ChurnFeatureType.MEMBER_ACTION))
                .execute();
    }

    private Short toShortScore(ChurnScoreCalculationResult result, ChurnFeatureType featureType) {
        // feature 합계
        int featureScore = result.contributions().stream()
                .filter(contribution -> contribution.featureType() == featureType)
                .mapToInt(ChurnFeatureContribution::appliedScore)
                .sum();

        return (short) featureScore;
    }

    private String writeRiskReasons(List<ChurnFeatureContribution> contributions) {
        try {
            return objectMapper.writeValueAsString(contributions);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("risk reasons serialization failed", e);
        }
    }
}
