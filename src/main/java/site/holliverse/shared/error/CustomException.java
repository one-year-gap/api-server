package site.holliverse.shared.error;

public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String field;
    private final String reason;

    // ErrorCode만 던지면: message/reason 모두 defaultMessage로 통일
    public CustomException(ErrorCode errorCode) {
        this(errorCode, null, null, null);
    }

    // ErrorCode + field만 던지면: reason/message는 defaultMessage로 통일
    public CustomException(ErrorCode errorCode, String field) {
        this(errorCode, field, null, null);
    }

    // ErrorCode + field + reason (필요하면 override). message는 defaultMessage
    public CustomException(ErrorCode errorCode, String field, String reason) {
        this(errorCode, field, reason, null);
    }

    /**
     * @param message  HTTP 응답의 최상단 message에 사용할 값 (null이면 defaultMessage)
     * @param reason   errorDetail.reason에 사용할 값 (null이면 defaultMessage)
     */
    public CustomException(ErrorCode errorCode, String field, String reason, String message) {
        super(message != null ? message : errorCode.defaultMessage());
        this.errorCode = errorCode;
        this.field = field;
        this.reason = reason != null ? reason : errorCode.defaultMessage();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getField() {
        return field;
    }

    public String getReason() {
        return reason;
    }
}