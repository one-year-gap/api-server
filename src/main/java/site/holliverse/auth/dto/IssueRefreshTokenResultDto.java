package site.holliverse.auth.dto;

/**
 * 리프레시 토큰 발급 결과 DTO.
 * refreshToken: 쿠키에 담아 내려줄 원문 토큰
 * refreshTokenExpiresInSeconds: 쿠키 만료 시간(초)
 */
public record IssueRefreshTokenResultDto(
        String refreshToken,
        long refreshTokenExpiresInSeconds
) {
}
