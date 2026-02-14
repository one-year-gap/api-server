package site.holliverse.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
// 로그인 실패 시 에러 응답 바디를 작성하는 핸들러
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public LoginFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // 실패 응답 형식 구성
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "fail");
        body.put("message", "로그인 실패");
        body.put("data", null);
        body.put("timestamp", Instant.now().toString());
        body.put("requestId", resolveRequestId(request));

        // 401 Unauthorized JSON 응답 반환
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    // 요청 헤더의 X-Request-Id를 우선 사용하고, 없으면 새 UUID 생성
    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return (requestId == null || requestId.isBlank()) ? UUID.randomUUID().toString() : requestId;
    }
}
