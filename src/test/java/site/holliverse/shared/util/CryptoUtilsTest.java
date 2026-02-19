package site.holliverse.shared.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptoUtilsTest {

    private EncryptionTool encryptionTool;
    private DecryptionTool decryptionTool;

    // 테스트용 설정값
    private final String SECRET_KEY = "12345678901234567890123456789012"; // 32byte
    private final String ALGORITHM = "AES";
    private final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    @BeforeEach
    void setUp() {
        // 스프링 도움 없이 직접 생성 (CryptoConfig가 하는 일을 수동으로 재현)
        // 두 도구가 "같은 키"와 "같은 알고리즘"을 쓰는지 확인
        encryptionTool = new EncryptionTool(SECRET_KEY, ALGORITHM, TRANSFORMATION);
        decryptionTool = new DecryptionTool(SECRET_KEY, ALGORITHM, TRANSFORMATION);
    }

    @Test
    @DisplayName("EncryptionTool로 암호화한 내용을 DecryptionTool로 복호화하면 원본과 같아야 한다.")
    void encrypt_and_decrypt_success() {
        // given
        String originalText = "010-1234-5678";

        // when
        // 1. 암호화 도구로 잠그고
        String encryptedText = encryptionTool.encrypt(originalText);
        // 2. 복호화 도구로 푼다
        String decryptedText = decryptionTool.decrypt(encryptedText);

        // then
        // 1. 암호문은 평문과 달라야 함 (암호화 확인)
        assertThat(encryptedText).isNotEqualTo(originalText);
        assertThat(encryptedText).isNotNull();

        // 2. 복호화된 텍스트는 원본과 정확히 일치해야 함 (복호화 확인)
        assertThat(decryptedText).isEqualTo(originalText);
    }

    @Test
    @DisplayName("null이나 빈 문자열이 입력되면 null을 반환해야 한다.")
    void null_safe_test() {
        // EncryptionTool 검증
        assertThat(encryptionTool.encrypt(null)).isNull();
        assertThat(encryptionTool.encrypt("")).isNull();

        // DecryptionTool 검증
        assertThat(decryptionTool.decrypt(null)).isNull();
        assertThat(decryptionTool.decrypt("")).isNull();
    }

    @Test
    @DisplayName("다른 비밀키를 가진 도구로 복호화를 시도하면 실패해야 한다.")
    void decrypt_fail_with_wrong_key() {
        // given
        String originalText = "My Secret Data";
        String encryptedText = encryptionTool.encrypt(originalText);

        // when: 키가 다른 복호화 도구 생성
        String wrongKey = "00000000000000000000000000000000"; // 틀린 키
        DecryptionTool wrongDecryptor = new DecryptionTool(wrongKey, ALGORITHM, TRANSFORMATION);

        // then: 복호화 시도 시 에러 발생 확인
        assertThatThrownBy(() -> wrongDecryptor.decrypt(encryptedText))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("복호화 실패: 보안 키 또는 알고리즘 설정이 잘못되었습니다.");
    }
}