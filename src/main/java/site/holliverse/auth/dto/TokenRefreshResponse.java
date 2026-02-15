package site.holliverse.auth.dto;

/**
 * 토큰 재발급 유스케이스 결과 DTO.
 * 컨트롤러/핸들러 정책에 따라 access/refresh를
 * 바디 또는 쿠키로 분리 전달할 수 있다.
 */
public record TokenRefreshResponse(
        /** 새로 발급된 액세스 토큰. */
        String accessToken,
        /** 액세스 토큰 만료 시간(초 단위). */
        long accessTokenExpiresIn,
        /** 새로 발급된 리프레시 토큰. */
        String refreshToken,
        /** 리프레시 토큰 만료 시간(초 단위). */
        long refreshTokenExpiresIn
) {
}