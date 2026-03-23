package site.holliverse.customer.error;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.holliverse.shared.error.ErrorCategory;
import site.holliverse.shared.error.ErrorCode;

@RequiredArgsConstructor
public enum CustomerErrorCode implements ErrorCode {

    INVALID_USER_LOG_EVENT_ID(HttpStatus.BAD_REQUEST, "CUS-VAL-001", ErrorCategory.VAL, "유효하지 않은 사용자 로그 이벤트 ID입니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CUS-DOM-001", ErrorCategory.DOM, "회원을 찾을 수 없습니다."),
    PERSONA_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "CUS-DOM-002", ErrorCategory.DOM, "페르소나 타입을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CUS-DOM-003", ErrorCategory.DOM, "상품을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CUS-DOM-004", ErrorCategory.DOM, "카테고리 마스터 데이터를 찾을 수 없습니다."),
    MOBILE_PLAN_ALREADY_SUBSCRIBED(HttpStatus.CONFLICT, "CUS-DOM-005", ErrorCategory.DOM, "이미 가입 중인 상품입니다."),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "CUS-DOM-006", ErrorCategory.DOM, "보유 쿠폰을 찾을 수 없습니다."),
    COUPON_ALREADY_USED(HttpStatus.CONFLICT, "CUS-DOM-007", ErrorCategory.DOM, "이미 사용된 쿠폰입니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "CUS-DOM-008", ErrorCategory.DOM, "만료된 쿠폰입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final ErrorCategory category;
    private final String message;

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

    @Override
    public String message() {
        return message;
    }
}
