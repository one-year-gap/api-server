package site.holliverse.auth.application.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.auth.jwt.RefreshTokenHashService;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.entity.RefreshToken;
import site.holliverse.shared.persistence.entity.enums.MemberStatusType;
import site.holliverse.shared.persistence.repository.MemberRepository;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
// 리프레시 토큰 검증 및 재발급 유스케이스
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

    @Transactional
    public Map<String, Object> refresh(String rawRefreshToken) {
        // 1) JWT 서명/만료/타입 검증
        if (!jwtTokenProvider.isValid(rawRefreshToken) || !jwtTokenProvider.isRefreshToken(rawRefreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        Long memberId = jwtTokenProvider.getMemberId(rawRefreshToken);
        String tokenHash = refreshTokenHashService.hash(rawRefreshToken);

        // 2) DB 해시 토큰 조회(폐기되지 않은 토큰만)
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found"));

        // 3) 토큰-회원 매핑 검증
        if (!refreshToken.getMemberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token owner mismatch");
        }

        // 4) 만료 토큰 차단 및 폐기
        if (refreshToken.isExpired()) {
            refreshToken.revoke();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        // 5) 회원 상태 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Member not found"));

        if (member.getStatus() != MemberStatusType.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Member status does not allow token refresh");
        }

        // 6) 액세스/리프레시 토큰 재발급(리프레시 회전)
        String newAccessToken = jwtTokenProvider.generateAccessToken(member);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        String newRefreshTokenHash = refreshTokenHashService.hash(newRefreshToken);
        Instant newRefreshExpiresAt = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());

        refreshToken.rotate(newRefreshTokenHash, newRefreshExpiresAt);

        // 7) 응답 데이터 구성
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", newAccessToken);
        data.put("accessTokenExpiresIn", jwtTokenProvider.getAccessTokenExpirationSeconds());
        data.put("refreshToken", newRefreshToken);
        data.put("refreshTokenExpiresIn", jwtTokenProvider.getRefreshTokenExpirationSeconds());
        return data;
    }
}
