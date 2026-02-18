package site.holliverse.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.holliverse.shared.util.DecryptionTool;
import site.holliverse.shared.util.EncryptionTool;

/**
 * 암호화/복호화 도구를 Spring Bean으로 등록하는 설정 클래스.
 * application.yml의 설정값을 주입받아 생성.
 */
@Configuration
public class CryptoConfig {

    @Value("${secret-key}")
    private String secretKey;

    @Value("${algorithm:AES}")
    private String algorithm;

    @Value("${transformation:AES/ECB/PKCS5Padding}")
    private String transformation;

    @Bean
    public EncryptionTool encryptionTool() {
        return new EncryptionTool(secretKey, algorithm, transformation);
    }

    @Bean
    public DecryptionTool decryptionTool() {
        return new DecryptionTool(secretKey, algorithm, transformation);
    }
}