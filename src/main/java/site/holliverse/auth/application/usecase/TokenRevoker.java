package site.holliverse.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.shared.persistence.entity.RefreshToken;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;

@Service
public class TokenRevoker {
    /**
     * RefreshToken 폐기를 " 별도 트랜잭션 으로 커밋하는 전용 컴포넌트
     *
     * 이 클래스 사용 이유 :
     * refresh 흐름은 @트랜잭셔널 안에서 동작하는데
     * 만료 토큰에서 revoke 후 예외처리가 나면 바깥 트랜잭션이 롤백된다.
     * 같은 트랜잭션에 있던 revoke 변경도 함께 롤백 되어 DB에 남지 않을 수도 있다.
     *
     * 목적 : 폐기만 별도 트랜잭션으로 즉시 커밋해서 이후 바깥 트랜잭션에서 예외가 나도 revoke상태를 유지한다.
     *
     *  REQUIRES_NEW는 프록시 경유 호출에서만 유효하므로 별도 빈으로 분리한다.
     *
     */

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenRevoker(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeById(Long refreshTokenId) {
        RefreshToken refreshToken = refreshTokenRepository.findById(refreshTokenId)
                .orElseThrow(() -> new IllegalArgumentException("refresh token not found: " + refreshTokenId));
        refreshToken.revoke();
    }
}
