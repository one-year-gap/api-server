package site.holliverse.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import site.holliverse.auth.error.AuthErrorCode;
import site.holliverse.shared.logging.LogFieldKeys;
import site.holliverse.shared.web.response.ApiErrorDetail;
import site.holliverse.shared.web.response.ApiErrorResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 로그인 실패 시 공통 에러 응답을 생성하는 핸들러.
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final Logger SECURITY_LOG = LoggerFactory.getLogger("APP_SECURITY");
    private final ObjectMapper objectMapper;

    public LoginFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        AuthErrorCode code = (exception instanceof AuthenticationServiceException)
                ? AuthErrorCode.INVALID_LOGIN_REQUEST
                : AuthErrorCode.INVALID_CREDENTIALS;

        //민감정보는 남기지 않음.
        MDC.put(LogFieldKeys.EVENT, "security.auth.failure");
        MDC.put(LogFieldKeys.SEVERITY, "warn");
        MDC.put(LogFieldKeys.ERROR_TYPE, exception.getClass().getName());
        MDC.put(LogFieldKeys.ERROR_CODE, code.code());
        MDC.put(LogFieldKeys.STATUS, String.valueOf(code.httpStatus().value()));

        try {
            SECURITY_LOG.warn("authentication failed");
        } finally {
            MDC.remove(LogFieldKeys.EVENT);
            MDC.remove(LogFieldKeys.SEVERITY);
            MDC.remove(LogFieldKeys.ERROR_TYPE);
            MDC.remove(LogFieldKeys.ERROR_CODE);
            MDC.remove(LogFieldKeys.STATUS);
        }

        ApiErrorResponse body = ApiErrorResponse.error(
                code.message(),
                new ApiErrorDetail(
                        code.code(),
                        null,
                        exception.getMessage()
                )
        );

        response.setStatus(code.httpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
