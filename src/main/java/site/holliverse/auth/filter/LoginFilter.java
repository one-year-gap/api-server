package site.holliverse.auth.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// /auth/login 요청(JSON email/password)을 인증 토큰으로 변환하는 필터
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    // 로그인 JSON 키(email, password)
    public static final String SPRING_SECURITY_FORM_EMAIL_KEY = "email";
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

    // 이 필터가 처리할 경로: POST /auth/login
    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.POST, "/auth/login");

    private String emailParameter = SPRING_SECURITY_FORM_EMAIL_KEY;
    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;

    public LoginFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        // 로그인 엔드포인트는 POST만 허용
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        Map<String, String> loginMap;

        try {
            // 요청 본문(JSON)에서 email/password 추출
            ObjectMapper objectMapper = new ObjectMapper();
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            loginMap = objectMapper.readValue(messageBody, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String email = loginMap.get(emailParameter);
        email = (email != null) ? email.trim() : "";
        String password = loginMap.get(passwordParameter);
        password = (password != null) ? password : "";

        // 인증 매니저가 처리할 미인증 토큰 생성
        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(email,
                password);
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    // 요청 메타정보(IP, 세션 등)를 인증 객체 details에 세팅
    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}
