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

/**
 * JSON 로그인 요청을 Spring Security 인증 토큰으로 변환하는 필터.
 * 처리 엔드포인트: POST /v1/auth/login
 */
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    /** 로그인 JSON에서 이메일 키 이름. */
    public static final String SPRING_SECURITY_FORM_EMAIL_KEY = "email";
    /** 로그인 JSON에서 비밀번호 키 이름. */
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

    /** 로그인 요청만 이 필터가 처리하도록 매칭합니다. */
    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.POST, "/v1/auth/login");

    private String emailParameter = SPRING_SECURITY_FORM_EMAIL_KEY;
    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;

    public LoginFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
    }

    /**
     * 로그인 JSON 본문을 읽어 AuthenticationManager에 인증을 위임한다.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        Map<String, String> loginMap;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            loginMap = objectMapper.readValue(messageBody, new TypeReference<>() {
            });
        } catch (IOException e) {
            // 파싱 실패는 보안 예외 흐름으로 전달되도록 런타임 예외로 전환
            throw new RuntimeException(e);
        }

        String email = loginMap.get(emailParameter);
        email = (email != null) ? email.trim() : "";
        String password = loginMap.get(passwordParameter);
        password = (password != null) ? password : "";

        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(email,
                password);
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * 요청 메타데이터(IP/세션 등)를 인증 토큰 details에 설정합니다.
     */
    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}