package site.holliverse.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.auth.jwt.RefreshTokenHashService;
import site.holliverse.shared.persistence.entity.RefreshToken;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;
import site.holliverse.shared.security.CustomUserDetails;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
// 로그인 성공 시 Access/Refresh 토큰 발급 및 응답 바디를 작성하는 핸들러
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHashService refreshTokenHashService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

    public LoginSuccessHandler(JwtTokenProvider jwtTokenProvider,
                               RefreshTokenHashService refreshTokenHashService,
                               RefreshTokenRepository refreshTokenRepository,
                               ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenHashService = refreshTokenHashService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 인증 결과 principal에서 사용자 정보 추출
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // Access/Refresh 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getMemberId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
        String refreshTokenRaw = jwtTokenProvider.generateRefreshToken(user.getMemberId());

        // DB에는 원문 대신 해시값만 저장
        String refreshTokenHash = refreshTokenHashService.hash(refreshTokenRaw);
        Instant refreshExpiresAt = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());

        // 회원당 활성 리프레시 토큰 1개 정책: 있으면 회전, 없으면 신규 저장
        refreshTokenRepository.findByMemberIdAndRevokedFalse(user.getMemberId())
                .ifPresentOrElse(
                        existing -> existing.rotate(refreshTokenHash, refreshExpiresAt),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .memberId(user.getMemberId())
                                .tokenHash(refreshTokenHash)
                                .expiresAt(refreshExpiresAt)
                                .revoked(false)
                                .build())
                );

        // 응답 data 블록 구성
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", accessToken);
        data.put("accessTokenExpiresIn", jwtTokenProvider.getAccessTokenExpirationSeconds());
        data.put("refreshToken", refreshTokenRaw);
        data.put("refreshTokenExpiresIn", jwtTokenProvider.getRefreshTokenExpirationSeconds());

        // 공통 응답 형식 구성
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("message", "로그인 성공");
        body.put("data", data);
        body.put("timestamp", Instant.now().toString());
        body.put("requestId", resolveRequestId(request));

        // JSON 응답 반환
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    // 요청 헤더의 X-Request-Id를 우선 사용하고, 없으면 새 UUID 생성
    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return (requestId == null || requestId.isBlank()) ? UUID.randomUUID().toString() : requestId;
    }
}
