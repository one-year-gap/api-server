package site.holliverse.admin.application.usecase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import site.holliverse.admin.error.AdminErrorCode;
import site.holliverse.admin.error.AdminException;
import site.holliverse.shared.error.SharedErrorCode;

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
    @JsonValue
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

    @JsonCreator
    public static UserActionFeatureEventName from(String value) {
        return find(value)
                .orElseThrow(() -> new AdminException(AdminErrorCode.INVALID_CLICK_VALUE));
    }
}
