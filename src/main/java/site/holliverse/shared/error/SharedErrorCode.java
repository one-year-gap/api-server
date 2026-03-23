package site.holliverse.shared.error;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SharedErrorCode implements ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "SHR-VAL-001", ErrorCategory.VAL, "유효성 검증에 실패했습니다."),
    MISSING_FIELD(HttpStatus.BAD_REQUEST, "SHR-VAL-002", ErrorCategory.VAL, "필수 입력값이 누락되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "SHR-VAL-003", ErrorCategory.VAL, "인증에 실패했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "SHR-DOM-001", ErrorCategory.DOM, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "SHR-DOM-002", ErrorCategory.DOM, "요청한 리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "SHR-DOM-003", ErrorCategory.DOM, "중복 또는 충돌이 발생했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SHR-INFRA-001", ErrorCategory.INFRA, "서버 내부 오류가 발생했습니다.");

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
