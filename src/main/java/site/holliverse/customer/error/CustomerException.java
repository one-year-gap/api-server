package site.holliverse.customer.error;

import site.holliverse.shared.error.DomainException;

public class CustomerException extends DomainException {
    public CustomerException(CustomerErrorCode errorCode) {
        super(errorCode);
    }

    public CustomerException(CustomerErrorCode errorCode, String field) {
        super(errorCode, field, null);
    }

    public CustomerException(CustomerErrorCode errorCode, String field, String reason) {
        super(errorCode, field, reason);
    }

    public CustomerException(CustomerErrorCode errorCode, String field, String reason, String message) {
        super(errorCode, field, reason, message);
    }
}
