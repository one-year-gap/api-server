package site.holliverse.shared.logging;

public final class LogMessageLimiter {
    public static final int MAX_LOG_MESSAGE_LENGTH = 5000;
    private static final String EMPTY_FALLBACK = "-";

    private LogMessageLimiter() {
    }

    public static String limit(String msg) {
        if (msg == null || msg.isBlank()) {
            return EMPTY_FALLBACK;
        }
        if (msg.length() <= MAX_LOG_MESSAGE_LENGTH) {
            return msg;
        }

        return msg.substring(0, MAX_LOG_MESSAGE_LENGTH) + "...";
    }
}
