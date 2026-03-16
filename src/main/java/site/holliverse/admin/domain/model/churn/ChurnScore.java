package site.holliverse.admin.domain.model.churn;

/**
 * 이탈률 점수.
 */
public record ChurnScore(int value) {

    public static final int MIN = 0;
    public static final int MAX = 100;

    public ChurnScore {
        if (value < MIN || value > MAX) {
            throw new IllegalStateException("이탈률 점수는 0 이상 100 이하만 가능합니다.");
        }
    }

    public static ChurnScore fromRaw(int rawValue) {
        int normalized = Math.max(MIN, Math.min(MAX, rawValue));
        return new ChurnScore(normalized);
    }
}
