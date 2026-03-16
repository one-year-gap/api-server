package site.holliverse.admin.domain.policy.churn;

import site.holliverse.admin.domain.model.churn.ChurnFeatureContribution;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.feature.ChurnFeature;

import java.util.List;

/**
 * 특정 feature를 점수 기여도로 변환.
 */
public interface ChurnFeatureScorer {

    ChurnFeatureType supports();

    List<ChurnFeatureContribution> contribute(ChurnFeature feature);

    default int score(ChurnFeature feature) {
        return contribute(feature).stream()
                .mapToInt(ChurnFeatureContribution::appliedScore)
                .sum();
    }
}
