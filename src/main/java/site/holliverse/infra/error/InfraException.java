package site.holliverse.infra.error;

import site.holliverse.shared.error.DomainException;

public class InfraException extends DomainException {
    public InfraException(InfraErrorCode errorCode) {
        super(errorCode);
    }

    public InfraException(InfraErrorCode errorCode, String field) {
        super(errorCode, field, null);
    }

    public InfraException(InfraErrorCode errorCode, String field, String reason) {
        super(errorCode, field, reason);
    }

    public InfraException(InfraErrorCode errorCode, String field, String reason, String message) {
        super(errorCode, field, reason, message);
    }
}
