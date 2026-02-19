package site.holliverse.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.holliverse.auth.application.usecase.IssueRefreshTokenUseCase;
import site.holliverse.auth.cookie.RefreshTokenCookieUtil;
import site.holliverse.auth.dto.AuthTokenResponseDto;
import site.holliverse.auth.dto.IssueRefreshTokenResultDto;
import site.holliverse.auth.dto.TokenRefreshResponseDto;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.shared.security.CustomUserDetails;
import site.holliverse.shared.web.response.ApiResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 일반 로그인 성공 핸들러.
 * 웹 계층에서는 인증 정보 추출/응답 작성만 담당한다.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final IssueRefreshTokenUseCase issueRefreshTokenUseCase;
    private final ObjectMapper objectMapper;

    public LoginSuccessHandler(JwtTokenProvider jwtTokenProvider,
                               IssueRefreshTokenUseCase issueRefreshTokenUseCase,
                               ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.issueRefreshTokenUseCase = issueRefreshTokenUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 액세스 토큰은 즉시 응답 바디(JSON)로 전달한다.
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getMemberId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );

        // 리프레시 토큰 발급/DB 저장은 유스케이스에 위임한다.
        IssueRefreshTokenResultDto issuedRefresh = issueRefreshTokenUseCase.issue(user.getMemberId());

        TokenRefreshResponseDto data = new TokenRefreshResponseDto(
                accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                issuedRefresh.refreshToken(),
                issuedRefresh.refreshTokenExpiresInSeconds()
        );

        // 리프레시 토큰은 HttpOnly 쿠키로 내려준다.
        RefreshTokenCookieUtil.addRefreshTokenCookie(
                response,
                data.refreshToken(),
                data.refreshTokenExpiresIn(),
                request.isSecure()
        );

        AuthTokenResponseDto bodyData = new AuthTokenResponseDto(
                data.accessToken(),
                "Bearer",
                data.accessTokenExpiresIn()
        );
        ApiResponse<AuthTokenResponseDto> body = ApiResponse.success("로그인 성공", bodyData);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
