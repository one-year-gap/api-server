package site.holliverse.admin.application.usecase;

import java.util.Arrays;
import java.util.Optional;

/**
 * 로그 이벤트명.
 */
public enum LogFeatureEventName {
    CLICK_COMPARE("click_compare"),
    CLICK_PENALTY("click_penalty");

    private final String value;

    LogFeatureEventName(String value) {
        this.value = value;
    }

    /**
     * 원본 값.
     */
    public String value() {
        return value;
    }

    /**
     * 이벤트 조회.
     */
    public static Optional<LogFeatureEventName> find(String value) {
        return Arrays.stream(values())
                .filter(eventName -> eventName.value.equals(value))
                .findFirst();
    }
}
