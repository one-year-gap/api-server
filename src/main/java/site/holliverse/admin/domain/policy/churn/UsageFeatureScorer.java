package site.holliverse.admin.domain.policy.churn;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.config.ChurnScoringProperties;
import site.holliverse.admin.domain.model.churn.ChurnFeatureContribution;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.ChurnSignalType;
import site.holliverse.admin.domain.model.churn.feature.ChurnFeature;
import site.holliverse.admin.domain.model.churn.feature.UsageFeature;

import java.util.List;

/**
 * 사용량 기반 feature 점수 계산.
 */
@Profile("admin")
@Component
@RequiredArgsConstructor
public class UsageFeatureScorer implements ChurnFeatureScorer {

    private final ChurnScoringProperties churnScoringProperties;
    private final ChurnScoreBandResolver churnScoreBandResolver;

    @Override
    public ChurnFeatureType supports() {
        return ChurnFeatureType.USAGE;
    }

    @Override
    public List<ChurnFeatureContribution> contribute(ChurnFeature feature) {
        UsageFeature usageFeature = (UsageFeature) feature;
        int usageScore = churnScoreBandResolver.resolveIntScore(
                churnScoringProperties.getRules().getUsage().getAllowanceUsageRatePct(),
                usageFeature.allowanceUsageRatePct()
        );

        return List.of(
                new ChurnFeatureContribution(
                        ChurnSignalType.ALLOWANCE_USAGE_RATE_PCT,
                        String.valueOf(usageFeature.allowanceUsageRatePct()),
                        usageScore
                )
        );
    }
}
