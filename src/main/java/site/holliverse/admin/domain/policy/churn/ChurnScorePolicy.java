package site.holliverse.admin.domain.policy.churn;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.domain.model.churn.ChurnFeatureContribution;
import site.holliverse.admin.domain.model.churn.ChurnFeatureSet;
import site.holliverse.admin.domain.model.churn.ChurnScore;
import site.holliverse.admin.domain.model.churn.ChurnScoreCalculationResult;
import site.holliverse.admin.domain.model.churn.feature.ChurnFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * feature별 점수 기여도를 합산해 최종 점수 계산.
 */
@Profile("admin")
@Component
@RequiredArgsConstructor
public class ChurnScorePolicy {

    private final ChurnFeatureScorerFactory churnFeatureScorerFactory;

    public ChurnScore calculate(ChurnFeatureSet featureSet) {
        return calculateDetails(featureSet).score();
    }

    public ChurnScoreCalculationResult calculateDetails(ChurnFeatureSet featureSet) {
        int totalScore = 0;
        List<ChurnFeatureContribution> contributions = new ArrayList<>();

        for (ChurnFeature feature : featureSet.features().values()) {
            ChurnFeatureScorer scorer = churnFeatureScorerFactory.get(feature.type());
            List<ChurnFeatureContribution> featureContributions = scorer.contribute(feature);
            contributions.addAll(featureContributions);
            totalScore += featureContributions.stream()
                    .mapToInt(ChurnFeatureContribution::appliedScore)
                    .sum();
        }

        return new ChurnScoreCalculationResult(ChurnScore.fromRaw(totalScore), contributions);
    }
}
