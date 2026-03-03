package site.holliverse.shared.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 400
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "유효성 검증에 실패했습니다."),
    MISSING_FIELD(HttpStatus.BAD_REQUEST, "MISSING_FIELD", "필수 입력값이 누락되었습니다."),
    OAUTH_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "OAUTH_INVALID_REQUEST", "OAuth 요청이 올바르지 않습니다."),
    OAUTH_USER_INFO_INVALID(HttpStatus.BAD_REQUEST, "OAUTH_USER_INFO_INVALID", "OAuth 사용자 정보가 유효하지 않습니다."),
    DATA_NOT_YET_ANALYZED(HttpStatus.BAD_REQUEST, "DATA_NOT_YET_ANALYZED", "해당 기간의 데이터 분석이 아직 완료되지 않았습니다."),

    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증에 실패했습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REVOKED", "폐기된 리프레시 토큰입니다."),
    REFRESH_TOKEN_OWNER_MISMATCH(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_OWNER_MISMATCH", "토큰 소유자가 일치하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "만료된 리프레시 토큰입니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED,"REFRESH_TOKEN_MISSING","리프레시 토큰이 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    OAUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "OAUTH_UNAUTHORIZED", "OAuth 인증에 실패했습니다."),

    // 403
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),

    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"MEMBER_NOT_FOUND","멤버를 찾을수가 없습니다."),

    // 409
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "DUPLICATED_EMAIL", "이미 사용 중인 이메일입니다."),
    DUPLICATED_PHONE(HttpStatus.CONFLICT, "DUPLICATED_PHONE", "이미 사용 중인 전화번호입니다."),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "중복 또는 충돌이 발생했습니다."),

    // 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
