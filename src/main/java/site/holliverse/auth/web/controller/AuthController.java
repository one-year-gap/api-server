package site.holliverse.auth.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.auth.application.usecase.AuthUseCase;
import site.holliverse.auth.application.usecase.RefreshTokenUseCase;
import site.holliverse.auth.cookie.RefreshTokenCookieUtil;
import site.holliverse.auth.dto.AuthTokenResponseDto;
import site.holliverse.auth.dto.SignUpDataResponseDto;
import site.holliverse.auth.dto.SignUpRequestDto;
import site.holliverse.auth.dto.TokenRefreshResponseDto;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.web.response.ApiResponse;

/**
 * 인증 관련 API 컨트롤러.
 * <p>
 * 제공 API:
 * - 회원가입
 * - 토큰 재발급
 * - 로그아웃
 */
@RestController
@RequestMapping
public class AuthController {

    private final AuthUseCase authUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(AuthUseCase authUseCase, RefreshTokenUseCase refreshTokenUseCase) {
        this.authUseCase = authUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    //가입
    @PostMapping("/api/v1/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignUpDataResponseDto> signUp(@Valid @RequestBody SignUpRequestDto request) {
        Long memberId = authUseCase.signUp(request).memberId();

        return ApiResponse.success(
                "회원가입이 완료되었습니다.",
                new SignUpDataResponseDto(memberId)
        );
    }

    /**
     * 리프레시 토큰 쿠키를 사용해 액세스 토큰을 재발급한다.
     * <p>
     * 성공 시 리프레시 쿠키도 회전(재설정)한다.
     */
    @PostMapping("/v1/auth/refresh")
    public ApiResponse<AuthTokenResponseDto> refresh(
            @CookieValue(name = RefreshTokenCookieUtil.COOKIE_NAME, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, null, "리프레시 토큰이 없습니다");
        }

        TokenRefreshResponseDto data = refreshTokenUseCase.refresh(refreshToken);
        RefreshTokenCookieUtil.addRefreshTokenCookie(
                response,
                data.refreshToken(),
                data.refreshTokenExpiresIn(),
                request.isSecure()
        );

        AuthTokenResponseDto bodyData = new AuthTokenResponseDto(
                data.accessToken(),
                "Bearer",
                data.accessTokenExpiresIn()
        );
        return ApiResponse.success("토큰 재발급 성공", bodyData);
    }

    /**
     * 현재 리프레시 토큰을 폐기하고 쿠키를 삭제한다.
     */
    @PostMapping("/v1/auth/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = RefreshTokenCookieUtil.COOKIE_NAME, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authUseCase.logout(refreshToken);
        RefreshTokenCookieUtil.clearRefreshTokenCookie(response, request.isSecure());
        return ApiResponse.success("로그아웃 성공", null);
    }
}