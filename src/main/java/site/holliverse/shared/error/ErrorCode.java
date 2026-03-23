package site.holliverse.shared.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus httpStatus();
    String code();
    ErrorCategory category();
    String message();
}
