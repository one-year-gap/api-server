package site.holliverse.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.holliverse.auth.cookie.RefreshTokenCookieUtil;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.auth.jwt.RefreshTokenHashService;
import site.holliverse.shared.persistence.entity.RefreshToken;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SocialSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHashService refreshTokenHashService;
    private final RefreshTokenRepository refreshTokenRepository;

    //프론트 성공시 리다이렉트 주소인데 Processing 일경우, Active 일경우 합의를 봐야한다.
    @Value("${app.oauth2.redirect-uri:http://localhost:8080/test/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // CustomOAuth2UserService에서 넣어준 내부 memberId를 principal attributes에서 꺼낸다.
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Long memberId = ((Number) oauth2User.getAttribute("memberId")).longValue();

        // 쿠키 전송에는 원문 토큰을 쓰고, DB에는 해시만 저장한다.
        String refreshTokenRaw = jwtTokenProvider.generateRefreshToken(memberId);
        String refreshTokenHash = refreshTokenHashService.hash(refreshTokenRaw);
        Instant refreshExpiresAt = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());

        // 회원당 활성 리프레시 토큰 1개 정책: 있으면 rotate, 없으면 신규 생성.
        refreshTokenRepository.findByMemberIdAndRevokedFalse(memberId)
                .ifPresentOrElse(
                        existing -> {
                            existing.rotate(refreshTokenHash, refreshExpiresAt);
                            refreshTokenRepository.save(existing);
                        },
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .memberId(memberId)
                                .tokenHash(refreshTokenHash)
                                .expiresAt(refreshExpiresAt)
                                .revoked(false)
                                .build())
                );

        // HttpOnly 리프레시 쿠키를 발급한 뒤 프론트 콜백으로 리다이렉트한다.
        RefreshTokenCookieUtil.addRefreshTokenCookie(
                response,
                refreshTokenRaw,
                jwtTokenProvider.getRefreshTokenExpirationSeconds(),
                request.isSecure() // HTTPS 요청이면 Secure 쿠키로 발급된다.
        );
        response.sendRedirect(redirectUri);
    }
}
