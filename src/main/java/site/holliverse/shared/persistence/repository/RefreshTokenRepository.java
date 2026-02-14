package site.holliverse.shared.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.shared.persistence.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 해시값 + 활성 상태로 리프레시 토큰 조회
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    // 회원의 활성 리프레시 토큰 1건 조회(단순 구현)
    Optional<RefreshToken> findByMemberIdAndRevokedFalse(Long memberId);

    // 회원의 리프레시 토큰 전체 폐기/정리에 사용 가능
    void deleteByMemberId(Long memberId);
}
