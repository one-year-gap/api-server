package site.holliverse.auth.dto;

// Access/Refresh 토큰 응답 데이터 DTO
public record TokenRefreshResponse(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) {
}
