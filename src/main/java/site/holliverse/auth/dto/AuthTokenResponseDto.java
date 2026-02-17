package site.holliverse.auth.dto;

/**
 * 로그인/재발급 성공 시 클라이언트에 전달하는 액세스 토큰 응답 DTO.
 * 리프레시 토큰은 HttpOnly 쿠키로 전달하므로 본문에서 제외한다.
 */
public record AuthTokenResponseDto(
        /** Authorization 헤더(Bearer)로 사용하는 액세스 토큰. */
        String accessToken,
        /** 토큰 타입 문자열(일반적으로 "Bearer"). */
        String tokenType,
        /** 액세스 토큰 만료 시간(초 단위). */
        long expiresIn
) {
}