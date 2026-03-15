package site.holliverse.admin.domain.policy.churn;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.config.ChurnScoringProperties;
import site.holliverse.admin.domain.model.churn.ChurnRiskGrade;

/**
 * 최종 점수 기준 등급 판정 정책.
 */
@Profile("admin")
@Component
@RequiredArgsConstructor
public class ChurnRiskGradePolicy {

    private final ChurnScoringProperties churnScoringProperties;

    public ChurnRiskGrade classify(int score) {
        if (score >= churnScoringProperties.getGrade().getHigh()) {
            return ChurnRiskGrade.HIGH;
        }

        if (score >= churnScoringProperties.getGrade().getMedium()) {
            return ChurnRiskGrade.MEDIUM;
        }

        return ChurnRiskGrade.LOW;
    }
}
