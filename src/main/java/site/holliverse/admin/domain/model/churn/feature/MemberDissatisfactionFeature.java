package site.holliverse.admin.domain.model.churn.feature;

import site.holliverse.admin.domain.model.churn.ConsultationSentimentType;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;

/**
 * 고객 불만 기반 feature.
 */
public record MemberDissatisfactionFeature(
        double starMeanScore,
        ConsultationSentimentType sentimentType,
        int maxKeywordNegativeWeight
) implements ChurnFeature {

    @Override
    public ChurnFeatureType type() {
        return ChurnFeatureType.MEMBER_DISSATISFACTION;
    }
}
