package site.holliverse.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.auth.dto.TokenRefreshResponseDto;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.auth.jwt.RefreshTokenHashService;
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.entity.RefreshToken;
import site.holliverse.shared.persistence.repository.MemberRepository;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;

import java.time.Instant;

/**
 * 리프레시 토큰 검증 및 토큰 재발급 유스케이스.
 * 보안 보장 사항:
 * - 리프레시 토큰 서명/타입/만료 검증
 * - DB에는 해시 토큰만 저장
 * - 토큰 소유자(memberId) 일치 검증
 * - 성공 시 리프레시 토큰 회전
 */
@Service
public class RefreshTokenUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHashService refreshTokenHashService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    public RefreshTokenUseCase(JwtTokenProvider jwtTokenProvider,
                               RefreshTokenHashService refreshTokenHashService,
                               RefreshTokenRepository refreshTokenRepository,
                               MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenHashService = refreshTokenHashService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * 전달받은 리프레시 토큰으로 액세스/리프레시 토큰을 재발급한다.
     */
    @Transactional
    public TokenRefreshResponseDto refresh(String rawRefreshToken) {
        // 1) JWT 서명/만료/토큰타입 검증
        if (!jwtTokenProvider.isValid(rawRefreshToken) || !jwtTokenProvider.isRefreshToken(rawRefreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, null, "Invalid refresh token");
        }

        Long memberId = jwtTokenProvider.getMemberId(rawRefreshToken);
        String tokenHash = refreshTokenHashService.hash(rawRefreshToken);

        // 2) 활성 상태의 해시 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED, null, "Refresh token not found"));

        // 3) 토큰 소유자 일치 여부 확인
        if (!refreshToken.getMemberId().equals(memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, null, "Refresh token owner mismatch");
        }

        // 4) 만료 토큰은 폐기 후 차단
        if (refreshToken.isExpired()) {
            refreshToken.revoke();
            throw new CustomException(ErrorCode.TOKEN_EXPIRED, null, "Refresh token expired");
        }

        // 5) 회원 상태 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "memberId", "Member not found"));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN,
                    "memberStatus",
                    "Member status does not allow token refresh"
            );
        }

        // 6) 새 토큰 발급 및 리프레시 토큰 회전
        String newAccessToken = jwtTokenProvider.generateAccessToken(member);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        String newRefreshTokenHash = refreshTokenHashService.hash(newRefreshToken);
        Instant newRefreshExpiresAt = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());

        refreshToken.rotate(newRefreshTokenHash, newRefreshExpiresAt);

        // 7) 컨트롤러/핸들러 전달용 토큰 페이로드 반환
        return new TokenRefreshResponseDto(
                newAccessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );
    }
}