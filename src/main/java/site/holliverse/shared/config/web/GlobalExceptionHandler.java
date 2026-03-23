package site.holliverse.shared.config.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.shared.error.ApiErrorResponseFactory;
import site.holliverse.shared.error.ConstraintExceptionMapper;
import site.holliverse.shared.error.ConstraintMapping;
import site.holliverse.shared.error.DomainException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.error.SharedErrorCode;
import site.holliverse.shared.logging.LogFieldKeys;
import site.holliverse.shared.web.response.ApiErrorDetail;
import site.holliverse.shared.web.response.ApiErrorResponse;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger APP_ERROR_LOG = LoggerFactory.getLogger("APP_ERROR");
    private static final int MAX_LOG_MESSAGE_LENGTH = 1024;
    private static final String UNKNOWN = "unknown";

    private final ApiErrorResponseFactory errorResponseFactory;
    private final ConstraintExceptionMapper constraintExceptionMapper;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomain(DomainException ex) {
        ApiErrorResponse body = errorResponseFactory.from(ex);
        logException(ex, ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().httpStatus()).body(body);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String field = fieldError != null ? fieldError.getField() : null;
        String reason = fieldError != null ? fieldError.getDefaultMessage() : SharedErrorCode.INVALID_INPUT.message();
        String message = reason != null ? reason : SharedErrorCode.INVALID_INPUT.message();
        ApiErrorDetail detail = new ApiErrorDetail(SharedErrorCode.INVALID_INPUT.code(), field, reason);
        ApiErrorResponse body = ApiErrorResponse.error(message, detail);
        logException(ex, SharedErrorCode.INVALID_INPUT, message);
        return ResponseEntity.status(SharedErrorCode.INVALID_INPUT.httpStatus()).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex) {
        String field = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() != null ? v.getPropertyPath().toString() : null)
                .orElse(null);
        String reason = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage() != null ? v.getMessage() : SharedErrorCode.INVALID_INPUT.message())
                .orElse(SharedErrorCode.INVALID_INPUT.message());
        ApiErrorDetail detail = new ApiErrorDetail(SharedErrorCode.INVALID_INPUT.code(), field, reason);
        ApiErrorResponse body = ApiErrorResponse.error(reason, detail);
        logException(ex, SharedErrorCode.INVALID_INPUT, reason);
        return ResponseEntity.status(SharedErrorCode.INVALID_INPUT.httpStatus()).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        String field = ex.getParameterName();
        String reason = ex.getParameterName() + " 파라미터는 필수입니다.";
        ApiErrorDetail detail = new ApiErrorDetail(SharedErrorCode.MISSING_FIELD.code(), field, reason);
        ApiErrorResponse body = ApiErrorResponse.error(reason, detail);
        logException(ex, SharedErrorCode.MISSING_FIELD, reason);
        return ResponseEntity.status(SharedErrorCode.MISSING_FIELD.httpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String field = ex.getName();
        String reason = field + " 값의 형식이 올바르지 않습니다.";
        ApiErrorDetail detail = new ApiErrorDetail(SharedErrorCode.INVALID_INPUT.code(), field, reason);
        ApiErrorResponse body = ApiErrorResponse.error(reason, detail);
        logException(ex, SharedErrorCode.INVALID_INPUT, reason);
        return ResponseEntity.status(SharedErrorCode.INVALID_INPUT.httpStatus()).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String constraintName = extractConstraintName(ex);
        ConstraintMapping mapping = constraintExceptionMapper.map(constraintName);
        return conflict(mapping.errorCode(), mapping.field(), mapping.errorCode().message(), ex);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex) {
        ApiErrorDetail detail = new ApiErrorDetail(
                SharedErrorCode.UNAUTHORIZED.code(),
                null,
                SharedErrorCode.UNAUTHORIZED.message()
        );
        ApiErrorResponse body = ApiErrorResponse.error(SharedErrorCode.UNAUTHORIZED.message(), detail);
        logException(ex, SharedErrorCode.UNAUTHORIZED, ex.getMessage());
        return ResponseEntity.status(SharedErrorCode.UNAUTHORIZED.httpStatus()).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ApiErrorDetail detail = new ApiErrorDetail(
                SharedErrorCode.FORBIDDEN.code(),
                null,
                SharedErrorCode.FORBIDDEN.message()
        );
        ApiErrorResponse body = ApiErrorResponse.error(SharedErrorCode.FORBIDDEN.message(), detail);
        logException(ex, SharedErrorCode.FORBIDDEN, ex.getMessage());
        return ResponseEntity.status(SharedErrorCode.FORBIDDEN.httpStatus()).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        ErrorCode errorCode = resolveErrorCode(ex.getStatusCode());
        String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        ApiErrorDetail detail = new ApiErrorDetail(ex.getStatusCode().toString(), null, message);
        ApiErrorResponse body = ApiErrorResponse.error(message, detail);
        logException(ex, errorCode, message);
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : SharedErrorCode.INVALID_INPUT.message();
        ApiErrorDetail detail = new ApiErrorDetail(SharedErrorCode.INVALID_INPUT.code(), null, message);
        ApiErrorResponse body = ApiErrorResponse.error(message, detail);
        logException(ex, SharedErrorCode.INVALID_INPUT, message);
        return ResponseEntity.status(SharedErrorCode.INVALID_INPUT.httpStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception ex) {
        ApiErrorDetail detail = new ApiErrorDetail(
                SharedErrorCode.INTERNAL_ERROR.code(),
                null,
                SharedErrorCode.INTERNAL_ERROR.message()
        );
        ApiErrorResponse body = ApiErrorResponse.error(SharedErrorCode.INTERNAL_ERROR.message(), detail);
        logException(ex, SharedErrorCode.INTERNAL_ERROR, ex.getMessage());
        return ResponseEntity.status(SharedErrorCode.INTERNAL_ERROR.httpStatus()).body(body);
    }

    private void logException(Throwable ex, ErrorCode status, String message) {
        String severity = resolveSeverity(status.httpStatus());
        boolean includeStackTrace = "error".equals(severity) || "fatal".equalsIgnoreCase(severity);

        ensureRequestContext(status.httpStatus());
        MDC.put(LogFieldKeys.SEVERITY, severity);
        MDC.put(LogFieldKeys.ERROR_TYPE, ex.getClass().getName());
        MDC.put(LogFieldKeys.ERROR_CODE, status.code());

        String safeMessage = message;
        if (safeMessage == null || safeMessage.isBlank()) {
            safeMessage = "error";
        }
        if (message != null && message.length() > MAX_LOG_MESSAGE_LENGTH) {
            safeMessage = message.substring(0, MAX_LOG_MESSAGE_LENGTH) + "...";
        }

        try {
            if (includeStackTrace) {
                APP_ERROR_LOG.error(safeMessage, ex);
            } else {
                APP_ERROR_LOG.warn(safeMessage);
            }
        } finally {
            MDC.remove(LogFieldKeys.SEVERITY);
            MDC.remove(LogFieldKeys.ERROR_TYPE);
            MDC.remove(LogFieldKeys.ERROR_CODE);
        }
    }

    private String resolveSeverity(HttpStatus status) {
        String existing = MDC.get(LogFieldKeys.SEVERITY);
        if ("fatal".equalsIgnoreCase(existing)) {
            return "fatal";
        }
        return status.is5xxServerError() ? "error" : "warn";
    }

    private void ensureRequestContext(HttpStatus status) {
        HttpServletRequest request = currentRequest();

        if (isBlank(MDC.get(LogFieldKeys.METHOD))) {
            MDC.put(LogFieldKeys.METHOD, request != null ? request.getMethod() : UNKNOWN);
        }
        if (isBlank(MDC.get(LogFieldKeys.URI_TEMPLATE))) {
            Object pattern = request != null
                    ? request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern")
                    : null;
            MDC.put(LogFieldKeys.URI_TEMPLATE, pattern != null ? pattern.toString() : UNKNOWN);
        }
        if (isBlank(MDC.get(LogFieldKeys.TRACE_ID))) {
            MDC.put(LogFieldKeys.TRACE_ID, UNKNOWN);
        }
        if (isBlank(MDC.get(LogFieldKeys.REQUEST_ID))) {
            MDC.put(LogFieldKeys.REQUEST_ID, UNKNOWN);
        }
        MDC.put(LogFieldKeys.STATUS, String.valueOf(status.value()));
    }

    private ResponseEntity<ApiErrorResponse> conflict(ErrorCode errorCode, String field, String reason, Exception ex) {
        ApiErrorDetail detail = new ApiErrorDetail(errorCode.code(), field, reason);
        ApiErrorResponse body = ApiErrorResponse.error(errorCode.message(), detail);
        logException(ex, errorCode, reason);
        return ResponseEntity.status(errorCode.httpStatus()).body(body);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String extractConstraintName(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (message == null || message.isBlank()) {
            return null;
        }

        String[] knownConstraints = {
                "uk_address_unique",
                "uk_member_email",
                "uk_member_phone",
                "uk_product_code",
                "uk_refresh_token_hash",
                "uk_category_group_category_name",
                "uk_business_keyword_code",
                "uk_billing_member_month",
                "uk_case_id",
                "uk_case_version",
                "uk_persona_type_name_version"
        };

        for (String constraint : knownConstraints) {
            if (message.contains(constraint)) {
                return constraint;
            }
        }

        return null;
    }

    private ErrorCode resolveErrorCode(HttpStatusCode statusCode) {
        if (statusCode.value() == HttpStatus.BAD_REQUEST.value()) {
            return SharedErrorCode.INVALID_INPUT;
        }
        if (statusCode.value() == HttpStatus.UNAUTHORIZED.value()) {
            return SharedErrorCode.UNAUTHORIZED;
        }
        if (statusCode.value() == HttpStatus.FORBIDDEN.value()) {
            return SharedErrorCode.FORBIDDEN;
        }
        if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
            return SharedErrorCode.NOT_FOUND;
        }
        if (statusCode.value() == HttpStatus.CONFLICT.value()) {
            return SharedErrorCode.CONFLICT;
        }
        return SharedErrorCode.INTERNAL_ERROR;
    }
}
