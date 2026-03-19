package site.holliverse.admin.application.usecase;

import java.util.Arrays;
import java.util.Optional;

/**
 * 로그 이벤트명.
 */
public enum UserActionFeatureEventName {
    CLICK_COMPARE("click_compare"),
    CLICK_PENALTY("click_penalty"),
    CLICK_CHANGE("click_change"),
    
    ;

    private final String value;

    UserActionFeatureEventName(String value) {
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
    public static Optional<UserActionFeatureEventName> find(String value) {
        return Arrays.stream(values())
                .filter(eventName -> eventName.value.equals(value))
                .findFirst();
    }
}
