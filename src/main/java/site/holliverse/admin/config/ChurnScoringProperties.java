package site.holliverse.admin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * churn scoring rule table 설정.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.churn")
public class ChurnScoringProperties {

    private Grade grade = new Grade();
    private Rules rules = new Rules();

    @Getter
    @Setter
    public static class Grade {
        private int high = 80;
        private int medium = 50;
    }

    @Getter
    @Setter
    public static class Rules {
        private Contract contract = new Contract();
        private Usage usage = new Usage();
        private MemberAction memberAction = new MemberAction();
        private MemberDissatisfaction memberDissatisfaction = new MemberDissatisfaction();
    }

    @Getter
    @Setter
    public static class Contract {
        private List<IntScoreBand> remainingWeeks = new ArrayList<>();
        private List<IntScoreBand> tenureWeeks = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Usage {
        private List<IntScoreBand> allowanceUsageRatePct = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class MemberAction {
        private List<IntScoreBand> changeMobileCount = new ArrayList<>();
        private List<IntScoreBand> comparisonCount = new ArrayList<>();
        private List<IntScoreBand> checkedPenaltyFeeCount = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class MemberDissatisfaction {
        private List<DecimalScoreBand> starMeanScore = new ArrayList<>();
        private Sentiment sentiment = new Sentiment();
    }

    @Getter
    @Setter
    public static class IntScoreBand {
        private Integer min;
        private Integer max;
        private int score;
    }

    @Getter
    @Setter
    public static class DecimalScoreBand {
        private BigDecimal min;
        private BigDecimal max;
        private int score;
    }

    @Getter
    @Setter
    public static class Sentiment {
        private int negative;
        private int positive;
        private int none;
    }
}
