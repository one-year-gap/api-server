package site.holliverse.shared.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 * AES-256 복호화 유틸리티.
 * Base64로 인코딩된 암호문을 받아 원본 문자열로 복원.
 */
public class DecryptionTool {
    private final String secretKey;
    private final String algorithm;
    private final String transformation;

    public DecryptionTool(String secretKey, String algorithm, String transformation) {
        this.secretKey = secretKey;
        this.algorithm = algorithm;
        this.transformation = transformation;
    }

    /**
     * 암호문을 복호화
     * @param cipherText Base64로 인코딩된 암호문
     * @return 복호화된 원본 문자열
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) return null;

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            // 1. 순수하게 암호화 로직(키, 알고리즘)이 터졌을 때
            throw new RuntimeException("복호화 실패: 보안 키 또는 알고리즘 설정이 잘못되었습니다.", e);

        } catch (IllegalArgumentException e) {
            // 2. Base64로 변환할 수 없는 이상한 문자열이 들어왔을 때
            throw new RuntimeException("복호화 실패: 올바른 Base64 인코딩 형식이 아닙니다.", e);
        }
    }
}