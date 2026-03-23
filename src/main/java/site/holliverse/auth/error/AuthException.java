package site.holliverse.auth.error;

import site.holliverse.shared.error.DomainException;

public class AuthException extends DomainException {
    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(AuthErrorCode errorCode, String field) {
        super(errorCode, field, null);
    }

    public AuthException(AuthErrorCode errorCode, String field, String reason) {
        super(errorCode, field, reason);
    }

    public AuthException(AuthErrorCode errorCode, String field, String reason, String message) {
        super(errorCode, field, reason, message);
    }
}
