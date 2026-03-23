package site.holliverse.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import site.holliverse.auth.error.AuthErrorCode;
import site.holliverse.shared.logging.LogFieldKeys;
import site.holliverse.shared.web.response.ApiErrorDetail;
import site.holliverse.shared.web.response.ApiErrorResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
// OAuth2 로그인 실패를 공통 에러 응답(JSON)으로 내려주는 핸들러
public class SocialFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;
    private static final Logger SECURITY_LOG = LoggerFactory.getLogger("APP_SECURITY");

    public SocialFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // Spring Security 예외를 우리 ErrorCode로 변환한다.
        AuthErrorCode errorCode = mapErrorCode(exception);
        ApiErrorResponse body = ApiErrorResponse.error(
                errorCode.message(),
                new ApiErrorDetail(errorCode.code(), null, exception.getMessage())
        );


        //social login error log 추가
        MDC.put(LogFieldKeys.EVENT, "security.auth.failure");
        MDC.put(LogFieldKeys.SEVERITY, "warn");
        MDC.put(LogFieldKeys.ERROR_TYPE, exception.getClass().getName());
        MDC.put(LogFieldKeys.ERROR_CODE, errorCode.code());
        MDC.put(LogFieldKeys.STATUS, String.valueOf(errorCode.httpStatus().value()));
        try {
            SECURITY_LOG.warn("social authentication failed");
        } finally {
            MDC.remove(LogFieldKeys.EVENT);
            MDC.remove(LogFieldKeys.SEVERITY);
            MDC.remove(LogFieldKeys.ERROR_TYPE);
            MDC.remove(LogFieldKeys.ERROR_CODE);
            MDC.remove(LogFieldKeys.STATUS);
        }

        // 프론트가 바로 처리할 수 있도록 JSON 형식으로 응답한다.
        response.setStatus(errorCode.httpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private AuthErrorCode mapErrorCode(AuthenticationException exception) {
        // OAuth2 예외가 아니면 인증 실패(401)로 처리한다.
        if (!(exception instanceof OAuth2AuthenticationException oauthEx)) {
            return AuthErrorCode.OAUTH_UNAUTHORIZED;
        }

        // OAuth2 표준/커스텀 error code를 서비스 공통 ErrorCode로 매핑한다.
        String oauthErrorCode = oauthEx.getError().getErrorCode();
        if ("invalid_request".equals(oauthErrorCode)) {
            return AuthErrorCode.OAUTH_INVALID_REQUEST;
        }
        if ("invalid_user_info".equals(oauthErrorCode)
                || "invalid_user_id".equals(oauthErrorCode)
                || "invalid_user_email".equals(oauthErrorCode)) {
            return AuthErrorCode.OAUTH_USER_INFO_INVALID;
        }
        if ("access_denied".equals(oauthErrorCode)) {
            return AuthErrorCode.OAUTH_UNAUTHORIZED;
        }

        return AuthErrorCode.OAUTH_UNAUTHORIZED;
    }
}
