package site.holliverse.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.auth.dto.IssueRefreshTokenResultDto;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.auth.jwt.RefreshTokenHashService;
import site.holliverse.shared.persistence.entity.RefreshToken;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;

import java.time.Instant;

/**
 * 로그인 성공 시점에 리프레시 토큰을 발급/회전하는 유스케이스.
 * 핸들러는 이 유스케이스만 호출하고, 저장 로직은 여기서 처리한다.
 */
@Service
public class IssueRefreshTokenUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHashService refreshTokenHashService;
    private final RefreshTokenRepository refreshTokenRepository;

    public IssueRefreshTokenUseCase(JwtTokenProvider jwtTokenProvider,
                                    RefreshTokenHashService refreshTokenHashService,
                                    RefreshTokenRepository refreshTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenHashService = refreshTokenHashService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * 회원의 리프레시 토큰을 발급하고 DB를 최신 상태로 맞춘다.
     * - 기존 활성 토큰이 있으면 rotate
     * - 없으면 신규 row 생성
     */
    @Transactional
    public IssueRefreshTokenResultDto issue(Long memberId) {
        String refreshTokenRaw = jwtTokenProvider.generateRefreshToken(memberId);
        String refreshTokenHash = refreshTokenHashService.hash(refreshTokenRaw);
        Instant refreshExpiresAt = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());

        refreshTokenRepository.findByMemberIdAndRevokedFalse(memberId)
                .ifPresentOrElse(
                        existing -> {
                            existing.rotate(refreshTokenHash, refreshExpiresAt);
                            refreshTokenRepository.save(existing);
                        },
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .memberId(memberId)
                                .tokenHash(refreshTokenHash)
                                .expiresAt(refreshExpiresAt)
                                .revoked(false)
                                .build())
                );

        return new IssueRefreshTokenResultDto(
                refreshTokenRaw,
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );
    }
}
