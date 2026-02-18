package site.holliverse.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.web.response.ApiErrorDetail;
import site.holliverse.shared.web.response.ApiErrorResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
// OAuth2 로그인 실패를 공통 에러 응답(JSON)으로 내려주는 핸들러
public class SocialFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public SocialFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // Spring Security 예외를 우리 ErrorCode로 변환한다.
        ErrorCode errorCode = mapErrorCode(exception);
        ApiErrorResponse body = ApiErrorResponse.error(
                errorCode.defaultMessage(),
                new ApiErrorDetail(errorCode.code(), null, exception.getMessage())
        );

        // 프론트가 바로 처리할 수 있도록 JSON 형식으로 응답한다.
        response.setStatus(errorCode.httpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private ErrorCode mapErrorCode(AuthenticationException exception) {
        // OAuth2 예외가 아니면 인증 실패(401)로 처리한다.
        if (!(exception instanceof OAuth2AuthenticationException oauthEx)) {
            return ErrorCode.OAUTH_UNAUTHORIZED;
        }

        // OAuth2 표준/커스텀 error code를 서비스 공통 ErrorCode로 매핑한다.
        String oauthErrorCode = oauthEx.getError().getErrorCode();
        if ("invalid_request".equals(oauthErrorCode)) {
            return ErrorCode.OAUTH_INVALID_REQUEST;
        }
        if ("invalid_user_info".equals(oauthErrorCode)
                || "invalid_user_id".equals(oauthErrorCode)
                || "invalid_user_email".equals(oauthErrorCode)) {
            return ErrorCode.OAUTH_USER_INFO_INVALID;
        }
        if ("access_denied".equals(oauthErrorCode)) {
            return ErrorCode.OAUTH_UNAUTHORIZED;
        }

        return ErrorCode.OAUTH_UNAUTHORIZED;
    }
}
