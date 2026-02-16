package site.holliverse.shared.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "refresh_token",
        indexes = {
                // 회원 기준 조회 최적화
                @Index(name = "idx_refresh_token_member_id", columnList = "member_id"),
                // 만료 시각 기준 조회 최적화
                @Index(name = "idx_refresh_token_expires_at", columnList = "expires_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 리프레시 토큰 저장 엔티티(원문 토큰이 아닌 해시값 저장)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    // 토큰 소유 회원 ID
    private Long memberId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    // 리프레시 토큰 원문의 해시값(SHA-256 Hex)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    // 토큰 만료 시각
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    // 폐기 여부(로그아웃/회전 시 사용)
    private boolean revoked;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    // 생성 시각
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    // 수정 시각
    private Instant updatedAt;

    @Builder
    private RefreshToken(Long memberId, String tokenHash, Instant expiresAt, boolean revoked) {
        this.memberId = memberId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    @PrePersist
        // 저장 전 필수 값 검증
    void prePersist() {
        if (expiresAt == null) {
            throw new IllegalStateException("refresh token expiresAt must not be null");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalStateException("refresh token hash must not be blank");
        }
    }

    // 토큰 폐기 처리
    public void revoke() {
        this.revoked = true;
    }

    // 토큰 회전(새 해시/만료시각으로 갱신)
    public void rotate(String tokenHash, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    // 만료 여부 확인
    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}