package site.holliverse.shared.error;

public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String field;
    private final String reason;

    public CustomException(ErrorCode errorCode) {
        this(errorCode, null, null, errorCode.defaultMessage());
    }

    public CustomException(ErrorCode errorCode, String field, String reason) {
        this(errorCode, field, reason, errorCode.defaultMessage());
    }

    public CustomException(ErrorCode errorCode, String field, String reason, String message) {
        super(message);
        this.errorCode = errorCode;
        this.field = field;
        this.reason = reason;
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
