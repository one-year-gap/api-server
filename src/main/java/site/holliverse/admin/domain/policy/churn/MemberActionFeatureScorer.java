package site.holliverse.admin.domain.policy.churn;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.config.ChurnScoringProperties;
import site.holliverse.admin.domain.model.churn.ChurnFeatureContribution;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.ChurnSignalType;
import site.holliverse.admin.domain.model.churn.feature.ChurnFeature;
import site.holliverse.admin.domain.model.churn.feature.MemberActionFeature;

import java.util.List;

/**
 * 회원 행동 기반 feature 점수 계산.
 */
@Profile("admin")
@Component
@RequiredArgsConstructor
public class MemberActionFeatureScorer implements ChurnFeatureScorer {

    private final ChurnScoringProperties churnScoringProperties;
    private final ChurnScoreBandResolver churnScoreBandResolver;

    @Override
    public ChurnFeatureType supports() {
        return ChurnFeatureType.MEMBER_ACTION;
    }

    @Override
    public List<ChurnFeatureContribution> contribute(ChurnFeature feature) {
        MemberActionFeature memberActionFeature = (MemberActionFeature) feature;
        int changeMobileScore = churnScoreBandResolver.resolveIntScore(
                churnScoringProperties.getRules().getMemberAction().getChangeMobileCount(),
                memberActionFeature.changeMobileCount()
        );
        int comparisonScore = churnScoreBandResolver.resolveIntScore(
                churnScoringProperties.getRules().getMemberAction().getComparisonCount(),
                memberActionFeature.comparisonCount()
        );
        int checkedPenaltyFeeScore = churnScoreBandResolver.resolveIntScore(
                churnScoringProperties.getRules().getMemberAction().getCheckedPenaltyFeeCount(),
                memberActionFeature.checkedPenaltyFeeCount()
        );

        return List.of(
                new ChurnFeatureContribution(
                        ChurnSignalType.CHANGE_MOBILE_COUNT,
                        String.valueOf(memberActionFeature.changeMobileCount()),
                        changeMobileScore
                ),
                new ChurnFeatureContribution(
                        ChurnSignalType.COMPARISON_COUNT,
                        String.valueOf(memberActionFeature.comparisonCount()),
                        comparisonScore
                ),
                new ChurnFeatureContribution(
                        ChurnSignalType.CHECKED_PENALTY_FEE_COUNT,
                        String.valueOf(memberActionFeature.checkedPenaltyFeeCount()),
                        checkedPenaltyFeeScore
                )
        );
    }
}
