package site.holliverse.admin.domain.policy.churn;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * feature 유형에 맞는 scorer 선택.
 */
@Profile("admin")
@Component
public class ChurnFeatureScorerFactory {

    private final Map<ChurnFeatureType, ChurnFeatureScorer> scorerMap;

    public ChurnFeatureScorerFactory(List<ChurnFeatureScorer> scorers) {
        Map<ChurnFeatureType, ChurnFeatureScorer> mappedScorers = new EnumMap<>(ChurnFeatureType.class);

        for (ChurnFeatureScorer scorer : scorers) {
            ChurnFeatureScorer previous = mappedScorers.putIfAbsent(scorer.supports(), scorer);

            if (previous != null) {
                throw new IllegalStateException("동일한 feature scorer가 중복 등록되었습니다. type=" + scorer.supports());
            }
        }

        this.scorerMap = Map.copyOf(mappedScorers);
    }

    public ChurnFeatureScorer get(ChurnFeatureType type) {
        ChurnFeatureScorer scorer = scorerMap.get(type);

        if (scorer == null) {
            throw new IllegalStateException("feature scorer가 없습니다. type=" + type);
        }

        return scorer;
    }
}
