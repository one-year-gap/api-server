package site.holliverse.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.entity.enums.MemberStatusType;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
// JWT 생성, 파싱, 검증을 담당하는 컴포넌트
public class JwtTokenProvider {

    // 토큰 타입 구분값
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    // 서명용 시크릿(app.jwt.secret)
    private final String secret;
    // Access Token 만료 시간(ms)
    private final long accessTokenExpirationMs;
    // Refresh Token 만료 시간(ms)
    private final long refreshTokenExpirationMs;
    // 실제 서명/검증에 사용하는 HMAC 키
    private SecretKey secretKey;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-ms:3600000}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-token-expiration-ms:1209600000}") long refreshTokenExpirationMs
    ) {
        this.secret = secret;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @PostConstruct
    // 애플리케이션 시작 시 시크릿 키 초기화
    void init() {
        // HS256 사용 시 최소 길이(32자) 검증
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Member 정보를 기반으로 Access Token 생성
    public String generateAccessToken(Member member) {
        return generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole().name(),
                member.getStatus()
        );
    }

    // principal 정보 기반으로 Access Token 생성
    public String generateAccessToken(Long memberId, String email, String role, MemberStatusType status) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(accessTokenExpirationMs);

        // subject: memberId, claims: email/role/status/tokenType
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("email", email)
                .claim("role", role)
                .claim("status", status.name())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성(subject에는 memberId만 저장)
    public String generateRefreshToken(Long memberId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    // 토큰을 파싱해 claims(payload) 반환
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 토큰에서 memberId(subject) 추출
    public Long getMemberId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    // 토큰에서 email claim 추출
    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    // 토큰에서 role claim 추출
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // Access Token 여부 확인
    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    // Refresh Token 여부 확인
    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    // 토큰 유효성 검증(서명, 만료)
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Access Token 만료 시간을 초 단위로 반환
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    // Refresh Token 만료 시간을 초 단위로 반환
    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationMs / 1000;
    }

    // Refresh Token 만료 시간을 ms 단위로 반환
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
