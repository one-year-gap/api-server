package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.admin.domain.model.churn.ChurnEvaluationResult;
import site.holliverse.admin.domain.model.churn.ChurnFeatureSet;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.ChurnRiskGrade;
import site.holliverse.admin.domain.model.churn.ChurnScoreCalculationResult;
import site.holliverse.admin.domain.model.churn.feature.MemberDissatisfactionFeature;
import site.holliverse.admin.domain.policy.churn.ChurnRiskGradePolicy;
import site.holliverse.admin.domain.policy.churn.ChurnScorePolicy;

import java.time.LocalDate;
import java.util.Map;

@Service
@Profile("admin")
@RequiredArgsConstructor
public class CalculateChurnScoreService {

    private final ChurnScorePolicy churnScorePolicy;
    private final ChurnRiskGradePolicy churnRiskGradePolicy;
    private final ChurnSnapshotStoreService churnSnapshotStoreService;

    public ChurnEvaluationResult calculateAndStore(
            Long memberId,
            LocalDate baseDate,
            MemberDissatisfactionFeature dissatisfactionFeature
    ) {
        // feature 조립
        ChurnFeatureSet featureSet = new ChurnFeatureSet(Map.of(
                ChurnFeatureType.MEMBER_DISSATISFACTION,
                dissatisfactionFeature
        ));

        // 점수 계산
        ChurnScoreCalculationResult scoreResult = churnScorePolicy.calculateDetails(featureSet);

        // 등급 계산
        ChurnRiskGrade riskGrade = churnRiskGradePolicy.classify(scoreResult.score().value());

        // 스냅샷 저장
        churnSnapshotStoreService.store(memberId, baseDate, scoreResult, riskGrade);

        // 결과 반환
        return new ChurnEvaluationResult(scoreResult, riskGrade);
    }
}
