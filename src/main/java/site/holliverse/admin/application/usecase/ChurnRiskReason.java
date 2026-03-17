package site.holliverse.admin.application.usecase;

import site.holliverse.admin.domain.model.churn.ChurnFeatureCollectionType;
import site.holliverse.admin.domain.model.churn.ChurnSignalType;

import java.util.List;
import java.util.Map;

/**
 * 이탈 위험 사유.
 */
public record ChurnRiskReason(
        Feature feature,
        ReasonCode reasonCode,
        String summary,
        int appliedScore,
        ChurnSignalType signalType,
        ChurnFeatureCollectionType collectionType,
        Object observedValue,
        Object evidence
) {
    /**
     * 위험 feature.
     */
    public enum Feature {
        BASE,
        USAGE,
        LOG,
        COUNSEL
    }

    /**
     * 위험 코드.
     */
    public enum ReasonCode {
        NEGATIVE_SENTIMENT("부정 상담 감지"),
        KEYWORD("상담 키워드 감지"),
        COMPARE("요금제 비교 클릭 감지"),
        CHECKED_PENALTY_FEE("위약금 확인 이력 클릭 감지");

        private final String defaultSummary;

        ReasonCode(String defaultSummary) {
            this.defaultSummary = defaultSummary;
        }

        /**
         * 기본 요약.
         */
        public String defaultSummary() {
            return defaultSummary;
        }

        /**
         * 감정 요약.
         */
        public String sentimentSummary(String consultationType) {
            if (consultationType == null || consultationType.isBlank()) {
                return defaultSummary;
            }

            return switch (consultationType) {
                case "NEGATIVE" -> "부정 상담 감지";
                case "POSITIVE" -> "긍정 상담 감지";
                default -> defaultSummary;
            };
        }

        /**
         * 키워드 요약.
         */
        public String keywordSummary(List<KeywordItem> keywords) {
            if (keywords == null || keywords.isEmpty()) {
                return defaultSummary;
            }

            List<String> labels = keywords.stream()
                    .limit(2)
                    .map(KeywordItem::summaryLabel)
                    .toList();

            if (keywords.size() > 2) {
                return String.join(", ", labels) + " 외 " + (keywords.size() - 2) + "건 키워드 감지";
            }

            return String.join(", ", labels) + " 키워드 감지";
        }

        /**
         * 로그 요약.
         */
        public String logSummary(int totalCount) {
            String label = switch (this) {
                case COMPARE -> "요금제 비교 클릭";
                case CHECKED_PENALTY_FEE -> "위약금 확인 이력 클릭";
                default -> defaultSummary;
            };

            return label + " 누적 " + totalCount + "회";
        }
    }

    /**
     * 감정 근거.
     */
    public record SentimentEvidence(
            Long caseId,
            String consultationType
    ) {
    }

    /**
     * 키워드 근거.
     */
    public record KeywordEvidence(
            List<KeywordItem> keywords
    ) {
    }

    /**
     * 키워드 항목.
     */
    public record KeywordItem(
            Long businessKeywordId,
            String keywordName,
            Integer count,
            Integer negativeWeight
    ) {
        /**
         * 항목 요약.
         */
        public String summaryLabel() {
            if (keywordName == null || keywordName.isBlank()) {
                return String.valueOf(businessKeywordId);
            }
            if (count == null || count <= 0) {
                return keywordName;
            }
            return keywordName + " " + count + "회";
        }
    }

    /**
     * 로그 근거.
     */
    public record LogEvidence(
            int incrementCount,
            int totalCount,
            List<LogEventItem> events
    ) {
    }

    /**
     * 로그 항목.
     */
    public record LogEventItem(
            Long eventId,
            String timestamp,
            String event,
            String eventName,
            Map<String, Object> eventProperties
    ) {
    }
}
