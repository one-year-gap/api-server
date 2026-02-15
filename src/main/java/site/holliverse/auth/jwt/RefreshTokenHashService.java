package site.holliverse.auth.jwt;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 원문 리프레시 토큰을 SHA-256 해시 문자열로 변환한다.
 * <p>
 * DB에는 원문 토큰 대신 해시값만 저장한다.
 */
@Component
public class RefreshTokenHashService {

    /**
     * 비어있지 않은 토큰을 SHA-256 해시(소문자 hex)로 반환한다.
     */
    public String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("refresh token must not be blank");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return toHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    /** byte 배열을 hex 문자열로 변환한다. */
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}