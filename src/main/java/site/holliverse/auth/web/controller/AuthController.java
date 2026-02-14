package site.holliverse.auth.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.auth.application.usecase.AuthUseCase;
import site.holliverse.auth.application.usecase.RefreshTokenUseCase;
import site.holliverse.auth.dto.RefreshTokenRequest;
import site.holliverse.auth.dto.SignUpDataResponse;
import site.holliverse.auth.dto.SignUpRequest;
import site.holliverse.auth.dto.TokenRefreshResponse;
import site.holliverse.shared.web.response.ApiResponse;

@RestController
@RequestMapping
public class AuthController {

    private final AuthUseCase authUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(AuthUseCase authUseCase, RefreshTokenUseCase refreshTokenUseCase) {
        this.authUseCase = authUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    // 회원가입 API
    @PostMapping("/api/v1/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignUpDataResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        Long memberId = authUseCase.signUp(request).memberId();

        return ApiResponse.success(
                "회원가입이 완료되었습니다.",
                new SignUpDataResponse(memberId)
        );
    }

    @PostMapping("/v1/auth/refresh")
    // 리프레시 토큰으로 액세스/리프레시 토큰 재발급 API
    public ApiResponse<TokenRefreshResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        TokenRefreshResponse data = refreshTokenUseCase.refresh(request.getRefreshToken());

        return ApiResponse.success("토큰 재발급 성공", data);
    }
}
