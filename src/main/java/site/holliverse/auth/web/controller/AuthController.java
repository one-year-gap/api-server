package site.holliverse.auth.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.auth.dto.RefreshTokenRequest;
import site.holliverse.auth.dto.SignUpRequest;
import site.holliverse.auth.dto.SingUpResponse;
import site.holliverse.auth.application.usecase.AuthUseCase;
import site.holliverse.auth.application.usecase.RefreshTokenUseCase;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(AuthUseCase authUseCase, RefreshTokenUseCase refreshTokenUseCase) {
        this.authUseCase = authUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @PostMapping("/signup")
    // 회원가입 API
    public ResponseEntity<SingUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authUseCase.signUp(request));
    }

    @PostMapping("/refresh")
    // 리프레시 토큰으로 액세스/리프레시 토큰 재발급 API
    public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        Map<String, Object> data = refreshTokenUseCase.refresh(request.getRefreshToken());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("message", "토큰 재발급 성공");
        body.put("data", data);
        body.put("timestamp", Instant.now().toString());
        body.put("requestId", UUID.randomUUID().toString());

        return ResponseEntity.ok(body);
    }
}
