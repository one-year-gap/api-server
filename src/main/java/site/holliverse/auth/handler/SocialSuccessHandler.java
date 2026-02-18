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
import site.holliverse.auth.application.usecase.IssueRefreshTokenUseCase;
import site.holliverse.auth.cookie.RefreshTokenCookieUtil;
import site.holliverse.auth.dto.IssueRefreshTokenResultDto;

import java.io.IOException;

/**
 * 소셜 로그인 성공 핸들러.
 * memberId 추출, 쿠키 설정, 리다이렉트만 담당한다.
 */
@Component
@RequiredArgsConstructor
public class SocialSuccessHandler implements AuthenticationSuccessHandler {

    private final IssueRefreshTokenUseCase issueRefreshTokenUseCase;

    // 소셜 로그인 성공 후 프론트로 이동할 주소
    @Value("${app.oauth2.redirect-uri:http://localhost:8080/test/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // OAuth2 principal에 넣어둔 내부 회원 ID를 꺼낸다.
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Long memberId = ((Number) oauth2User.getAttribute("memberId")).longValue();

        // 리프레시 토큰 발급/회전은 유스케이스에서 처리한다.
        IssueRefreshTokenResultDto issuedRefresh = issueRefreshTokenUseCase.issue(memberId);

        RefreshTokenCookieUtil.addRefreshTokenCookie(
                response,
                issuedRefresh.refreshToken(),
                issuedRefresh.refreshTokenExpiresInSeconds(),
                request.isSecure()
        );

        response.sendRedirect(redirectUri);
    }
}
