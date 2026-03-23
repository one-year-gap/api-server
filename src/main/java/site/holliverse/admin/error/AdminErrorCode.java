package site.holliverse.admin.error;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.holliverse.shared.error.ErrorCategory;
import site.holliverse.shared.error.ErrorCode;

@RequiredArgsConstructor
public enum AdminErrorCode implements ErrorCode {

    INVALID_MEMBER_STATUS(HttpStatus.BAD_REQUEST, "ADM-VAL-001", ErrorCategory.VAL, "유효하지 않은 회원 상태값입니다."),
    INVALID_MEMBERSHIP(HttpStatus.BAD_REQUEST, "ADM-VAL-002", ErrorCategory.VAL, "유효하지 않은 멤버십 등급입니다."),
    INVALID_YYYYMM(HttpStatus.BAD_REQUEST, "ADM-VAL-003", ErrorCategory.VAL, "yyyymm 값이 올바르지 않습니다."),
    INVALID_CLICK_VALUE(HttpStatus.BAD_REQUEST,"ADM-VAL-004",ErrorCategory.VAL,"log event명이 올바르지 않습니다."),

    DATA_NOT_YET_ANALYZED(HttpStatus.BAD_REQUEST, "ADM-APP-001", ErrorCategory.APP, "해당 기간의 데이터 분석이 아직 완료되지 않았습니다."),
    COUPON_EXPIRATION_DATE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "ADM-APP-002", ErrorCategory.APP, "쿠폰 만료일 정보를 확인할 수 없습니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ADM-DOM-001", ErrorCategory.DOM, "회원을 찾을 수 없습니다."),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "ADM-DOM-002", ErrorCategory.DOM, "존재하지 않는 쿠폰입니다.");

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
