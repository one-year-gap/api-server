package site.holliverse.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.web.response.ApiErrorDetail;
import site.holliverse.shared.web.response.ApiErrorResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 로그인 실패 시 공통 에러 응답을 생성하는 핸들러.
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public LoginFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        ApiErrorResponse body = ApiErrorResponse.error(
                ErrorCode.INVALID_CREDENTIALS.defaultMessage(),
                new ApiErrorDetail(
                        ErrorCode.INVALID_CREDENTIALS.code(),
                        null,
                        exception.getMessage()
                )
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}