package site.holliverse.admin.domain.policy.churn;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.config.ChurnScoringProperties;
import site.holliverse.admin.domain.model.churn.ChurnFeatureContribution;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.ChurnSignalType;
import site.holliverse.admin.domain.model.churn.feature.ChurnFeature;
import site.holliverse.admin.domain.model.churn.feature.MemberDissatisfactionFeature;

import java.util.List;

/**
 * 고객 불만 기반 feature 점수 계산.
 */
@Profile("admin")
@Component
@RequiredArgsConstructor
public class MemberDissatisfactionFeatureScorer implements ChurnFeatureScorer {

    private final ChurnScoringProperties churnScoringProperties;
    private final ChurnScoreBandResolver churnScoreBandResolver;

    @Override
    public ChurnFeatureType supports() {
        return ChurnFeatureType.MEMBER_DISSATISFACTION;
    }

    @Override
    public List<ChurnFeatureContribution> contribute(ChurnFeature feature) {
        MemberDissatisfactionFeature dissatisfactionFeature = (MemberDissatisfactionFeature) feature;
        int starMeanScore = churnScoreBandResolver.resolveDecimalScore(
                churnScoringProperties.getRules().getMemberDissatisfaction().getStarMeanScore(),
                dissatisfactionFeature.starMeanScore()
        );
        int sentimentScore = resolveSentimentScore(dissatisfactionFeature);
        int keywordNegativeWeightScore = Math.max(dissatisfactionFeature.maxKeywordNegativeWeight(), 0);

        return List.of(
                new ChurnFeatureContribution(
                        ChurnSignalType.STAR_MEAN_SCORE,
                        String.valueOf(dissatisfactionFeature.starMeanScore()),
                        starMeanScore
                ),
                new ChurnFeatureContribution(
                        ChurnSignalType.CONSULTATION_SENTIMENT,
                        dissatisfactionFeature.sentimentType().name(),
                        sentimentScore
                ),
                new ChurnFeatureContribution(
                        ChurnSignalType.MAX_KEYWORD_NEGATIVE_WEIGHT,
                        String.valueOf(dissatisfactionFeature.maxKeywordNegativeWeight()),
                        keywordNegativeWeightScore
                )
        );
    }

    private int resolveSentimentScore(MemberDissatisfactionFeature dissatisfactionFeature) {
        return switch (dissatisfactionFeature.sentimentType()) {
            case NEGATIVE -> churnScoringProperties.getRules().getMemberDissatisfaction().getSentiment().getNegative();
            case POSITIVE -> churnScoringProperties.getRules().getMemberDissatisfaction().getSentiment().getPositive();
            case NONE -> churnScoringProperties.getRules().getMemberDissatisfaction().getSentiment().getNone();
        };
    }
}
