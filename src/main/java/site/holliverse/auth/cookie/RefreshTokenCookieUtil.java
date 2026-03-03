package site.holliverse.auth.cookie;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * 리프레시 토큰 쿠키 생성/삭제 유틸리티.
 * <p>
 * 설계 의도:
 * - HttpOnly로 JS 접근 차단
 * - refresh 엔드포인트 경로로만 전송 제한
 */
public final class RefreshTokenCookieUtil {

    /** 로그인/재발급/로그아웃에서 사용하는 쿠키 키. */
    public static final String COOKIE_NAME = "refreshToken";
    /** 쿠키 전송을 refresh 경로로 제한하는 path 값. */
    private static final String COOKIE_PATH = "/v1/auth/refresh";

    private RefreshTokenCookieUtil() {
    }

    /**
     * HttpOnly 리프레시 토큰 쿠키를 응답 헤더에 추가한다.
     */
    public static void addRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken,
            long refreshTokenExpiresInSeconds,
            boolean secure
    ) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path(COOKIE_PATH)
                .sameSite("Lax")
                .maxAge(Duration.ofSeconds(refreshTokenExpiresInSeconds))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 리프레시 토큰 쿠키를 즉시 만료시켜 삭제한다.
     */
    public static void clearRefreshTokenCookie(HttpServletResponse response, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path(COOKIE_PATH)
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}