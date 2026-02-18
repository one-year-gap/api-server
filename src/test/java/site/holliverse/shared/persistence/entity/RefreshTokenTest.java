package site.holliverse.shared.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenTest {

    @Test
    @DisplayName("prePersist: expiresAt 이 null이면 예외가 발생한다")
    void prePersistThrowsWhenExpiresAtIsNull() {
        RefreshToken token = RefreshToken.builder()
                .memberId(1L)
                .tokenHash("hash")
                .expiresAt(null)
                .revoked(false)
                .build();

        assertThatThrownBy(token::prePersist)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("refresh token expiresAt must not be null");
    }

    @Test
    @DisplayName("prePersist: tokenHash 가 blank 면 예외가 발생한다")
    void prePersistThrowsWhenTokenHashIsBlank() {
        RefreshToken token = RefreshToken.builder()
                .memberId(1L)
                .tokenHash(" ")
                .expiresAt(Instant.now().plusSeconds(60))
                .revoked(false)
                .build();

        assertThatThrownBy(token::prePersist)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("refresh token hash must not be blank");
    }

    @Test
    @DisplayName("prePersist: 유효한 값이면 예외가 발생하지 않는다")
    void prePersistDoesNotThrowWithValidValues() {
        RefreshToken token = RefreshToken.builder()
                .memberId(1L)
                .tokenHash("hash")
                .expiresAt(Instant.now().plusSeconds(60))
                .revoked(false)
                .build();

        assertThatCode(token::prePersist).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("revoke: 토큰을 폐기 상태로 변경한다")
    void revokeMarksTokenAsRevoked() {
        RefreshToken token = RefreshToken.builder()
                .memberId(1L)
                .tokenHash("hash")
                .expiresAt(Instant.now().plusSeconds(60))
                .revoked(false)
                .build();

        token.revoke();

        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("rotate: hash, 만료시간을 갱신하고 revoked 를 false 로 돌린다")
    void rotateUpdatesFieldsAndClearsRevoked() {
        RefreshToken token = RefreshToken.builder()
                .memberId(1L)
                .tokenHash("old-hash")
                .expiresAt(Instant.now().plusSeconds(60))
                .revoked(true)
                .build();

        Instant newExpiresAt = Instant.now().plusSeconds(3600);
        token.rotate("new-hash", newExpiresAt);

        assertThat(token.getTokenHash()).isEqualTo("new-hash");
        assertThat(token.getExpiresAt()).isEqualTo(newExpiresAt);
        assertThat(token.isRevoked()).isFalse();
    }

    @Test
    @DisplayName("isExpired: 만료 시각 기준으로 true/false 를 반환한다")
    void isExpiredReturnsExpectedValue() {
        RefreshToken expired = RefreshToken.builder()
                .memberId(1L)
                .tokenHash("expired-hash")
                .expiresAt(Instant.now().minusSeconds(5))
                .revoked(false)
                .build();

        RefreshToken active = RefreshToken.builder()
                .memberId(1L)
                .tokenHash("active-hash")
                .expiresAt(Instant.now().plusSeconds(5))
                .revoked(false)
                .build();

        assertThat(expired.isExpired()).isTrue();
        assertThat(active.isExpired()).isFalse();
    }
}
