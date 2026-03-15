package site.holliverse.admin.domain.policy.churn;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.config.ChurnScoringProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * 설정된 점수 밴드에서 score를 해석
 */
@Profile("admin")
@Component
public class ChurnScoreBandResolver {

    public int resolveIntScore(List<ChurnScoringProperties.IntScoreBand> bands, int value) {
        for (ChurnScoringProperties.IntScoreBand band : bands) {
            if (matchesInt(band, value)) {
                return band.getScore();
            }
        }

        return 0;
    }

    public int resolveDecimalScore(List<ChurnScoringProperties.DecimalScoreBand> bands, double value) {
        BigDecimal decimalValue = BigDecimal.valueOf(value);

        for (ChurnScoringProperties.DecimalScoreBand band : bands) {
            if (matchesDecimal(band, decimalValue)) {
                return band.getScore();
            }
        }

        return 0;
    }

    private boolean matchesInt(ChurnScoringProperties.IntScoreBand band, int value) {
        boolean minMatched = band.getMin() == null || value >= band.getMin();
        boolean maxMatched = band.getMax() == null || value <= band.getMax();
        return minMatched && maxMatched;
    }

    private boolean matchesDecimal(ChurnScoringProperties.DecimalScoreBand band, BigDecimal value) {
        boolean minMatched = band.getMin() == null || value.compareTo(band.getMin()) >= 0;
        boolean maxMatched = band.getMax() == null || value.compareTo(band.getMax()) <= 0;
        return minMatched && maxMatched;
    }
}
