package site.holliverse.admin.web.dto.churn;

import java.util.Arrays;
import java.util.List;

public record ChurnRealTimeRequestDto(
        Integer limit,
        Long afterId,
        String level
) {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final List<String> DEFAULT_LEVELS = List.of("HIGH", "MEDIUM");

    /**
     * 최대 100건 제한
     */
    public int normalizedLimit() {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    /**
     * 최초 호출이나 비정상 값은 0
     */
    public long normalizedAfterId() {
        return afterId == null || afterId < 0 ? 0L : afterId;
    }

    /**
     * 외부 level 쿼리를 내부 DB 위험도 값으로 정규화
     */
    public List<String> riskLevels() {
        if (level == null || level.isBlank()) {
            return DEFAULT_LEVELS;
        }

        List<String> riskLevels = Arrays.stream(level.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toUpperCase)
                .filter(value -> value.equals("HIGH") || value.equals("MEDIUM"))
                .distinct()
                .toList();

        return riskLevels.isEmpty() ? DEFAULT_LEVELS : riskLevels;
    }
}
