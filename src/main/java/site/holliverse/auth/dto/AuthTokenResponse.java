package site.holliverse.auth.dto;

public record AuthTokenResponse(
        // 클라이언트가 Authorization 헤더에 담아 보낼 액세스 토큰
        String accessToken,
        // 토큰 타입(Bearer)
        String tokenType,
        // 토큰 만료 시간(초)
        long expiresIn
) {
}
