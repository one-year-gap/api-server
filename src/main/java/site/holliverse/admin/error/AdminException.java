package site.holliverse.admin.error;

import site.holliverse.shared.error.DomainException;

public class AdminException extends DomainException {
    public AdminException(AdminErrorCode errorCode) {
        super(errorCode);
    }

    public AdminException(AdminErrorCode errorCode, String field) {
        super(errorCode, field, null);
    }

    public AdminException(AdminErrorCode errorCode, String field, String reason) {
        super(errorCode, field, reason);
    }

    public AdminException(AdminErrorCode errorCode, String field, String reason, String message) {
        super(errorCode, field, reason, message);
    }
}
