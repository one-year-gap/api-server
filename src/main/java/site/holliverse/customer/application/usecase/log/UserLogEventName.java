package site.holliverse.customer.application.usecase.log;

import java.util.Arrays;

/**
 * 허용되는 사용자 이벤트 이름 정의.
 * 외부에서 들어오는 문자열 event_name을 열거형으로 매핑해 검증한다.
 */
public enum UserLogEventName {

    CLICK_PRODUCT_DETAIL("click_product_detail"),
    CLICK_LIST_TYPE("click_list_type"),
    CLICK_COMPARE("click_compare"),
    CLICK_CHANGE("click_change"),
    CLICK_CHANGE_SUCCESS("click_change_success"),
    CLICK_PENALTY("click_penalty"),
    CLICK_COUPON("click_coupon");

    private final String value;

    UserLogEventName(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static UserLogEventName from(String value) {
        return Arrays.stream(values())
                .filter(e -> e.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("허용되지 않는 event_name: " + value));
    }
}

