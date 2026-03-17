package site.holliverse.admin.application.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.domain.model.churn.ChurnFeatureContribution;
import site.holliverse.admin.domain.model.churn.ChurnScoreCalculationResult;
import site.holliverse.admin.domain.model.churn.ChurnSignalType;

import java.util.Optional;

/**
 * 위험 사유 생성기.
 */
@Profile("admin")
@Component
public class ChurnRiskReasonFactory {

    /**
     * 사유 생성.
     */
    public Optional<ChurnRiskReason> create(
            ChurnScoreCalculationResult scoreResult,
            ChurnRiskReason.Feature feature,
            ChurnRiskReason.ReasonCode reasonCode,
            ChurnSignalType signalType,
            Object observedValue,
            String summary,
            Object evidence
    ) {
        int appliedScore = appliedScore(scoreResult, signalType);
        if (appliedScore <= 0) {
            return Optional.empty();
        }

        return Optional.of(new ChurnRiskReason(
                feature,
                reasonCode,
                summary,
                appliedScore,
                signalType,
                signalType.getCollectionType(),
                observedValue,
                evidence
        ));
    }

    /**
     * signal 점수.
     */
    private int appliedScore(ChurnScoreCalculationResult scoreResult, ChurnSignalType signalType) {
        return scoreResult.contributions().stream()
                .filter(contribution -> contribution.signalType() == signalType)
                .mapToInt(ChurnFeatureContribution::appliedScore)
                .findFirst()
                .orElse(0);
    }
}
