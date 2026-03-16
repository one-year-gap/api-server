package site.holliverse.admin.domain.model.churn.feature;

import site.holliverse.admin.domain.model.churn.ChurnFeatureType;

/**
 * 계약 기반 feature.
 */
public record ContractFeature(
        int contractRemainingWeeks,
        int tenureWeeks
) implements ChurnFeature {

    @Override
    public ChurnFeatureType type() {
        return ChurnFeatureType.CONTRACT;
    }
}
