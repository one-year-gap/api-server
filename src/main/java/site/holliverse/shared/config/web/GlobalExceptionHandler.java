package site.holliverse.shared.config.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.logging.LogFieldKeys;
import site.holliverse.shared.web.response.ApiErrorDetail;
import site.holliverse.shared.web.response.ApiErrorResponse;
import org.springframework.validation.BindException;

import java.util.Objects;


@RestControllerAdvice
// 전역 예외를 공통 실패 응답 포맷(ApiErrorResponse)으로 변환한다.
public class GlobalExceptionHandler {
    private static final Logger APP_ERROR_LOG = LoggerFactory.getLogger("APP_ERROR");
    private static final int MAX_LOG_MESSAGE_LENGTH = 1024;
    private static final String UNKNOWN = "unknown";

    @ExceptionHandler(CustomException.class)
    // 서비스 계층에서 의도적으로 던진 도메인/비즈니스 예외 처리
    public ResponseEntity<ApiErrorResponse> handleBusiness(CustomException ex) {
        ErrorCode code = ex.getErrorCode();

        ApiErrorDetail detail = new ApiErrorDetail(
                code.code(),
                ex.getField(),
                ex.getReason()
        );

        ApiErrorResponse body = ApiErrorResponse.error(ex.getMessage(), detail);
        logException(ex, ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(code.httpStatus()).body(body);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    // @Valid + @RequestBody 검증 실패(JSON body validation)
    public ResponseEntity<ApiErrorResponse> handleValidation(BindException ex) {
        FieldError fe = ex.getBindingResult().getFieldError();

        String field = fe != null ? fe.getField() : "unknown";
        String reason = fe != null ? Objects.toString(fe.getDefaultMessage(), "입력값이 올바르지 않습니다.") : "입력값이 올바르지 않습니다.";

        ApiErrorResponse body = ApiErrorResponse.error(
                ErrorCode.INVALID_INPUT.defaultMessage(),
                new ApiErrorDetail(ErrorCode.INVALID_INPUT.code(), field, reason)
        );

        logException(ex, ErrorCode.INVALID_INPUT, ex.getMessage());

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.httpStatus()).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    // @Validated + QueryParam/PathVariable 검증 실패
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex) {
        ApiErrorResponse body = ApiErrorResponse.error(
                ErrorCode.INVALID_INPUT.defaultMessage(),
                new ApiErrorDetail(ErrorCode.INVALID_INPUT.code(), null, ex.getMessage())
        );
        logException(ex, ErrorCode.INVALID_INPUT, ex.getMessage());

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.httpStatus()).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    // 필수 요청 파라미터 누락
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        ApiErrorResponse body = ApiErrorResponse.error(
                ErrorCode.INVALID_INPUT.defaultMessage(),
                new ApiErrorDetail(ErrorCode.INVALID_INPUT.code(), ex.getParameterName(), ex.getMessage())
        );
        logException(ex, ErrorCode.INVALID_INPUT, ex.getMessage());

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.httpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    // PathVariable/QueryParam 타입 불일치
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiErrorResponse body = ApiErrorResponse.error(
                ErrorCode.INVALID_INPUT.defaultMessage(),
                new ApiErrorDetail(ErrorCode.INVALID_INPUT.code(), ex.getName(), ex.getMessage())
        );
        logException(ex, ErrorCode.INVALID_INPUT, ex.getMessage());

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.httpStatus()).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    // DB 유니크/제약조건 위반을 도메인 충돌 에러(409)로 매핑
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = (ex.getMostSpecificCause() != null) ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        msg = msg != null ? msg : "";

        if (msg.contains("uk_member_email")) {
            return conflict(ErrorCode.DUPLICATED_EMAIL, "email", ErrorCode.DUPLICATED_EMAIL.defaultMessage());
        }
        if (msg.contains("uk_member_phone")) {
            return conflict(ErrorCode.DUPLICATED_PHONE, "phone", ErrorCode.DUPLICATED_PHONE.defaultMessage());
        }

        logException(ex, ErrorCode.CONFLICT, ex.getMessage());
        return conflict(ErrorCode.CONFLICT, null, ErrorCode.CONFLICT.defaultMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    // 인증 실패(401)
    public ResponseEntity<ApiErrorResponse> handleAuth(AuthenticationException ex) {
        ApiErrorResponse body = ApiErrorResponse.error(
                ErrorCode.UNAUTHORIZED.defaultMessage(),
                new ApiErrorDetail(ErrorCode.UNAUTHORIZED.code(), null, ex.getMessage())
        );
        logException(ex,ErrorCode.UNAUTHORIZED,ex.getMessage());

        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.httpStatus()).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    // 인가 실패(403)
    public ResponseEntity<ApiErrorResponse> handleDenied(AccessDeniedException ex) {
        ApiErrorResponse body = ApiErrorResponse.error(
                ErrorCode.FORBIDDEN.defaultMessage(),
                new ApiErrorDetail(ErrorCode.FORBIDDEN.code(), null, ex.getMessage())
        );
        logException(ex,ErrorCode.FORBIDDEN,ex.getMessage());

        return ResponseEntity.status(ErrorCode.FORBIDDEN.httpStatus()).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    // 컨트롤러에서 던진 HTTP 상태 전용 예외(401, 400 등)를 그대로 반영
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        ErrorCode errorCode = resolveErrorCode(ex.getStatusCode());
        String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        ApiErrorDetail detail = new ApiErrorDetail(
                ex.getStatusCode().toString(),
                null,
                message
        );
        ApiErrorResponse body = ApiErrorResponse.error(message, detail);
        logException(ex,errorCode,ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    // UseCase 등에서 던진 비즈니스 규칙/입력 오류 → 400으로 응답 (500이 아닌 클라이언트 오류로 분류)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ApiErrorResponse body = ApiErrorResponse.error(
                ErrorCode.INVALID_INPUT.defaultMessage(),
                new ApiErrorDetail(ErrorCode.INVALID_INPUT.code(), null, ex.getMessage())
        );
        logException(ex,ErrorCode.INVALID_INPUT,ex.getMessage());

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.httpStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    // 미처 분류되지 않은 예외를 최종적으로 500으로 처리
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception ex) {
        ApiErrorResponse body = ApiErrorResponse.error(
                ErrorCode.INTERNAL_ERROR.defaultMessage(),
                new ApiErrorDetail(ErrorCode.INTERNAL_ERROR.code(), null, "예기치 못한 오류가 발생했습니다.")
        );
        logException(ex, ErrorCode.INTERNAL_ERROR, ex.getMessage());

        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.httpStatus()).body(body);
    }

    // 409 계열 응답 생성을 공통화하는 내부 헬퍼
    private ResponseEntity<ApiErrorResponse> conflict(ErrorCode code, String field, String reason) {
        ApiErrorResponse body = ApiErrorResponse.error(
                code.defaultMessage(),
                new ApiErrorDetail(code.code(), field, reason)
        );
        return ResponseEntity.status(code.httpStatus()).body(body);
    }

    /*
     * 예외처리 log
     */
    private void logException(Throwable ex, ErrorCode status, String message) {
        String severity = resolveSeverity(status.httpStatus());
        boolean includeStackTrace = "error".equals(severity) || "fatal".equalsIgnoreCase(severity);

        //필드 강제
        ensureRequestContext(status.httpStatus());
        MDC.put(LogFieldKeys.SEVERITY, severity);
        MDC.put(LogFieldKeys.ERROR_TYPE, ex.getClass().getName());
        MDC.put(LogFieldKeys.ERROR_CODE, status.code());

        String safeMessage = message;
        //유효성 검증
        if (safeMessage == null || safeMessage.isBlank()) {
            safeMessage = "error";
        }

        //메세지 길이 검증
        if (message != null && message.length() > MAX_LOG_MESSAGE_LENGTH) {
            safeMessage = message.substring(0, MAX_LOG_MESSAGE_LENGTH) + "...";
        }

        try {
            if (includeStackTrace) {
                //ERROR 이상만 stackTrace
                APP_ERROR_LOG.error(safeMessage, ex);
            } else {
                //WARN은 stackTrace 미포함
                APP_ERROR_LOG.warn(safeMessage);
            }
        } finally {
            MDC.remove(LogFieldKeys.SEVERITY);
            MDC.remove(LogFieldKeys.ERROR_TYPE);
            MDC.remove(LogFieldKeys.ERROR_CODE);
        }
    }

    /*
     * Error log level fatal 판단
     */
    private String resolveSeverity(HttpStatus status) {
        String existing = MDC.get(LogFieldKeys.SEVERITY);
        if ("fatal".equalsIgnoreCase(existing)) {
            return "fatal";
        }

        return status.is5xxServerError() ? "error" : "warn";
    }

    /*
     * MDC 필드가 비어있지 않게 조정
     */
    private void ensureRequestContext(HttpStatus status) {
        HttpServletRequest request = currentRequest();

        //method 보정
        if (hasText(MDC.get(LogFieldKeys.METHOD))) {
            MDC.put(LogFieldKeys.METHOD, request != null ? request.getMethod() : UNKNOWN);
        }
        //uriTemplate 보정
        if (hasText(MDC.get(LogFieldKeys.URI_TEMPLATE))) {
            Object pattern = request != null
                    ? request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern")
                    : null;
            MDC.put(LogFieldKeys.URI_TEMPLATE, pattern != null ? pattern.toString() : UNKNOWN);
        }
        //traceId 보정
        if (hasText(MDC.get(LogFieldKeys.TRACE_ID))) {
            MDC.put(LogFieldKeys.TRACE_ID, UNKNOWN);
        }
        //requestId 보정
        if (hasText(MDC.get(LogFieldKeys.REQUEST_ID))) {
            MDC.put(LogFieldKeys.REQUEST_ID, UNKNOWN);
        }
        MDC.put(LogFieldKeys.STATUS, String.valueOf(status.value()));
    }


    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private boolean hasText(String value) {
        return value == null || value.isBlank();
    }

    private ErrorCode resolveErrorCode(HttpStatusCode statusCode) {
      int s = statusCode.value();

      if (s == 400) return ErrorCode.INVALID_INPUT;
      if (s == 401) return ErrorCode.UNAUTHORIZED;
      if (s == 403) return ErrorCode.FORBIDDEN;
      if (s == 404) return ErrorCode.NOT_FOUND;
      if (s == 409) return ErrorCode.CONFLICT;
      if (s >= 500) return ErrorCode.INTERNAL_ERROR;

      // 정의되지 않은 4xx/기타 상태 fallback
      return (s >= 400 && s < 500) ? ErrorCode.INVALID_INPUT : ErrorCode.INTERNAL_ERROR;
  }
}
