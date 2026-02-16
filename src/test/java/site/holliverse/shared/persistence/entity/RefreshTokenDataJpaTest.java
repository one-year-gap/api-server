package site.holliverse.shared.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RefreshTokenDataJpaTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("저장 후 active token 조회(findByTokenHashAndRevokedFalse)가 동작한다")
    void saveAndFindActiveTokenByHash() {
        RefreshToken token = RefreshToken.builder()
                .memberId(1L)
                .tokenHash("hash-1")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        refreshTokenRepository.saveAndFlush(token);

        assertThat(refreshTokenRepository.findByTokenHashAndRevokedFalse("hash-1")).isPresent();
    }

    @Test
    @DisplayName("revoked=true 토큰은 active 조회에서 제외된다")
    void revokedTokenIsExcludedFromActiveQuery() {
        RefreshToken token = RefreshToken.builder()
                .memberId(1L)
                .tokenHash("hash-2")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        token.revoke();

        refreshTokenRepository.saveAndFlush(token);

        assertThat(refreshTokenRepository.findByTokenHashAndRevokedFalse("hash-2")).isEmpty();
    }

    @Test
    @DisplayName("tokenHash unique 제약 조건이 적용된다")
    void tokenHashUniqueConstraintIsEnforced() {
        RefreshToken first = RefreshToken.builder()
                .memberId(1L)
                .tokenHash("same-hash")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        RefreshToken second = RefreshToken.builder()
                .memberId(2L)
                .tokenHash("same-hash")
                .expiresAt(Instant.now().plusSeconds(7200))
                .revoked(false)
                .build();

        refreshTokenRepository.saveAndFlush(first);

        assertThatThrownBy(() -> refreshTokenRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("생성/수정 타임스탬프가 저장 및 갱신 시 채워진다")
    void auditTimestampsArePopulatedOnPersistAndUpdate() {
        RefreshToken token = RefreshToken.builder()
                .memberId(3L)
                .tokenHash("hash-3")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        RefreshToken saved = refreshTokenRepository.saveAndFlush(token);
        Instant createdAt = saved.getCreatedAt();
        Instant updatedAt = saved.getUpdatedAt();

        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();

        saved.rotate("hash-3-new", Instant.now().plusSeconds(7200));
        RefreshToken updated = refreshTokenRepository.saveAndFlush(saved);

        assertThat(updated.getUpdatedAt().isBefore(updatedAt)).isFalse();
    }
}
