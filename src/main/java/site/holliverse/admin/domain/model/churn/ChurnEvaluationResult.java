package site.holliverse.admin.domain.model.churn;

/**
 * 이탈률 계산 결과.
 */
public record ChurnEvaluationResult(
        ChurnScoreCalculationResult scoreResult,
        ChurnRiskGrade riskGrade
) {
}
