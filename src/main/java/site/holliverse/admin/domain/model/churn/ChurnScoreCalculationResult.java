package site.holliverse.admin.domain.model.churn;

import java.util.List;

/**
 * 점수 계산 상세 결과.
 */
public record ChurnScoreCalculationResult(
        ChurnScore score,
        List<ChurnFeatureContribution> contributions
) {

    public ChurnScoreCalculationResult {
        contributions = contributions == null
                ? List.of()
                : List.copyOf(contributions);
    }

    public int scoreByCollectionType(ChurnFeatureCollectionType collectionType) {
        return contributions.stream()
                .filter(contribution -> contribution.collectionType() == collectionType)
                .mapToInt(ChurnFeatureContribution::appliedScore)
                .sum();
    }
}
