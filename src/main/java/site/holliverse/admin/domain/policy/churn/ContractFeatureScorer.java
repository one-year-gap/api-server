package site.holliverse.admin.domain.policy.churn;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.config.ChurnScoringProperties;
import site.holliverse.admin.domain.model.churn.ChurnFeatureContribution;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.ChurnSignalType;
import site.holliverse.admin.domain.model.churn.feature.ChurnFeature;
import site.holliverse.admin.domain.model.churn.feature.ContractFeature;

import java.util.List;

/**
 * 계약 기반 feature 점수 계산.
 */
@Profile("admin")
@Component
@RequiredArgsConstructor
public class ContractFeatureScorer implements ChurnFeatureScorer {

    private final ChurnScoringProperties churnScoringProperties;
    private final ChurnScoreBandResolver churnScoreBandResolver;

    @Override
    public ChurnFeatureType supports() {
        return ChurnFeatureType.CONTRACT;
    }

    @Override
    public List<ChurnFeatureContribution> contribute(ChurnFeature feature) {
        ContractFeature contractFeature = (ContractFeature) feature;
        int remainingWeeksScore = churnScoreBandResolver.resolveIntScore(
                churnScoringProperties.getRules().getContract().getRemainingWeeks(),
                contractFeature.contractRemainingWeeks()
        );
        int tenureWeeksScore = churnScoreBandResolver.resolveIntScore(
                churnScoringProperties.getRules().getContract().getTenureWeeks(),
                contractFeature.tenureWeeks()
        );

        return List.of(
                new ChurnFeatureContribution(
                        ChurnSignalType.CONTRACT_REMAINING_WEEKS,
                        String.valueOf(contractFeature.contractRemainingWeeks()),
                        remainingWeeksScore
                ),
                new ChurnFeatureContribution(
                        ChurnSignalType.TENURE_WEEKS,
                        String.valueOf(contractFeature.tenureWeeks()),
                        tenureWeeksScore
                )
        );
    }
}
