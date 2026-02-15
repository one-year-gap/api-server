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

/**
 * JWT 발급/파싱/검증을 담당하는 중앙 컴포넌트.
 */
@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final String secret;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
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

    /**
     * 애플리케이션 시작 시 HMAC 서명 키를 초기화한다.
     */
    @PostConstruct
    void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Member member) {
        return generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole().name(),
                member.getStatus()
        );
    }

    /**
     * 사용자 식별/권한 정보를 포함한 Access Token을 발급한다.
     */
    public String generateAccessToken(Long memberId, String email, String role, MemberStatusType status) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(accessTokenExpirationMs);

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

    /**
     * 최소 정보(subject + tokenType)만 포함한 Refresh Token을 발급한다.
     */
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

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getMemberId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    /**
     * 서명/포맷/만료를 포함한 JWT 유효성 검증 결과를 반환한다.
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationMs / 1000;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}