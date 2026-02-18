package site.holliverse.shared.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-256 암호화 유틸리티.
 * 문자열을 입력받아 AES-256 알고리즘으로 암호화 후 Base64 문자열로 반환.
 */
public class EncryptionTool {
    private final String secretKey;
    private final String algorithm;
    private final String transformation;

    public EncryptionTool(String secretKey, String algorithm, String transformation) {
        this.secretKey = secretKey;
        this.algorithm = algorithm;
        this.transformation = transformation;
    }

    /**
     * 평문을 암호화
     * @param plainText 암호화할 원본 문자열 (예: "김영현")
     * @return Base64로 인코딩된 암호문 (예: "X8v...")
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) return null;

        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
}