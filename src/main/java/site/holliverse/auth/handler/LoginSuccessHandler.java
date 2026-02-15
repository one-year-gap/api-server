package site.holliverse.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.holliverse.auth.cookie.RefreshTokenCookieUtil;
import site.holliverse.auth.dto.AuthTokenResponse;
import site.holliverse.auth.dto.TokenRefreshResponse;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.auth.jwt.RefreshTokenHashService;
import site.holliverse.shared.persistence.entity.RefreshToken;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;
import site.holliverse.shared.security.CustomUserDetails;
import site.holliverse.shared.web.response.ApiResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * 로그인 성공 시 토큰 발급 및 응답 작성을 담당하는 핸들러.
 * <p>
 * 동작:
 * - access/refresh 토큰 발급
 * - DB 리프레시 토큰 해시 저장(또는 회전)
 * - access 토큰은 JSON 바디로 반환
 * - refresh 토큰은 HttpOnly 쿠키로 반환
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHashService refreshTokenHashService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

    public LoginSuccessHandler(JwtTokenProvider jwtTokenProvider,
                               RefreshTokenHashService refreshTokenHashService,
                               RefreshTokenRepository refreshTokenRepository,
                               ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenHashService = refreshTokenHashService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getMemberId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
        String refreshTokenRaw = jwtTokenProvider.generateRefreshToken(user.getMemberId());

        String refreshTokenHash = refreshTokenHashService.hash(refreshTokenRaw);
        Instant refreshExpiresAt = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());

        refreshTokenRepository.findByMemberIdAndRevokedFalse(user.getMemberId())
                .ifPresentOrElse(
                        existing -> {
                            // 중요:
                            // 과거에는 rotate()만 호출하고 save를 하지 않아,
                            // 쿠키에는 새 refresh token이 내려갔지만 DB hash는 이전 값으로 남는 문제가 있었다.
                            // 그 결과 refresh API에서 "Refresh token not found"가 발생했기 때문에,
                            // 회전 후에는 반드시 save로 변경 사항을 반영해 DB와 쿠키를 동기화해야 한다.
                            existing.rotate(refreshTokenHash, refreshExpiresAt);
                            refreshTokenRepository.save(existing);
                        },
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .memberId(user.getMemberId())
                                .tokenHash(refreshTokenHash)
                                .expiresAt(refreshExpiresAt)
                                .revoked(false)
                                .build())
                );

        TokenRefreshResponse data = new TokenRefreshResponse(
                accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                refreshTokenRaw,
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );

        RefreshTokenCookieUtil.addRefreshTokenCookie(
                response,
                data.refreshToken(),
                data.refreshTokenExpiresIn(),
                request.isSecure()
        );

        AuthTokenResponse bodyData = new AuthTokenResponse(
                data.accessToken(),
                "Bearer",
                data.accessTokenExpiresIn()
        );
        ApiResponse<AuthTokenResponse> body = ApiResponse.success("로그인 성공", bodyData);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
