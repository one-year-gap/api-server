package site.holliverse.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
// 토큰 재발급 요청 DTO
public class RefreshTokenRequest {

    @NotBlank
    // 클라이언트가 전달한 리프레시 토큰 원문
    private String refreshToken;
}
