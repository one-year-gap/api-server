package site.holliverse.admin.domain.model.churn.feature;

import site.holliverse.admin.domain.model.churn.ChurnFeatureType;

/**
 * 사용량 기반 feature.
 */
public record UsageFeature(
        int allowanceUsageRatePct
) implements ChurnFeature {

    @Override
    public ChurnFeatureType type() {
        return ChurnFeatureType.USAGE;
    }
}
