package site.holliverse.admin.domain.model.churn.feature;

import site.holliverse.admin.domain.model.churn.ChurnFeatureType;

/**
 * 회원 행동 기반 feature.
 */
public record MemberActionFeature(
        int changeMobileCount,
        int comparisonCount,
        int checkedPenaltyFeeCount
) implements ChurnFeature {

    @Override
    public ChurnFeatureType type() {
        return ChurnFeatureType.MEMBER_ACTION;
    }
}
