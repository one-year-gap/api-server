package site.holliverse.auth.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.auth.dto.TokenRefreshResponse;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.auth.jwt.RefreshTokenHashService;
import site.holliverse.shared.domain.model.MemberRole;
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.entity.RefreshToken;
import site.holliverse.shared.persistence.repository.MemberRepository;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenHashService refreshTokenHashService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    @Test
    @DisplayName("리프레시 토큰 재발급 성공")
    void refreshSuccess() {
        // 준비
        String rawRefreshToken = "raw-refresh-token";
        Long memberId = 1L;

        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(memberId)
                .tokenHash("old-hash")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        Member member = Member.builder()
                .id(memberId)
                .email("test@holliverse.com")
                .name("tester")
                .role(MemberRole.CUSTOMER)
                .status(MemberStatus.ACTIVE)
                .build();

        when(jwtTokenProvider.isValid(rawRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(rawRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getMemberId(rawRefreshToken)).thenReturn(memberId);
        when(refreshTokenHashService.hash(rawRefreshToken)).thenReturn("old-hash");
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse("old-hash"))
                .thenReturn(Optional.of(refreshToken));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        when(jwtTokenProvider.generateAccessToken(member)).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(memberId)).thenReturn("new-refresh-token");
        when(refreshTokenHashService.hash("new-refresh-token")).thenReturn("new-hash");
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(120000L);
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtTokenProvider.getRefreshTokenExpirationSeconds()).thenReturn(120L);

        // 실행
        TokenRefreshResponse result = refreshTokenUseCase.refresh(rawRefreshToken);

        // 검증
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(result.accessTokenExpiresIn()).isEqualTo(3600L);
        assertThat(result.refreshTokenExpiresIn()).isEqualTo(120L);
        assertThat(refreshToken.getTokenHash()).isEqualTo("new-hash");
        assertThat(refreshToken.isRevoked()).isFalse();
    }

    @Test
    @DisplayName("JWT가 유효하지 않으면 UNAUTHORIZED 예외 발생")
    void throwsWhenJwtInvalid() {
        // 준비
        String rawRefreshToken = "invalid-token";
        when(jwtTokenProvider.isValid(rawRefreshToken)).thenReturn(false);

        // 실행, 검증
        assertThatThrownBy(() -> refreshTokenUseCase.refresh(rawRefreshToken))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException custom = (CustomException) ex;
                    assertThat(custom.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
                });

        verifyNoInteractions(refreshTokenRepository, memberRepository);
    }

    @Test
    @DisplayName("리프레시 토큰이 만료되면 TOKEN_EXPIRED 예외 발생")
    void throwsWhenRefreshTokenExpired() {
        // 준비
        String rawRefreshToken = "expired-token";
        Long memberId = 1L;

        RefreshToken expiredToken = RefreshToken.builder()
                .memberId(memberId)
                .tokenHash("expired-hash")
                .expiresAt(Instant.now().minusSeconds(10))
                .revoked(false)
                .build();

        when(jwtTokenProvider.isValid(rawRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(rawRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getMemberId(rawRefreshToken)).thenReturn(memberId);
        when(refreshTokenHashService.hash(rawRefreshToken)).thenReturn("expired-hash");
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse("expired-hash"))
                .thenReturn(Optional.of(expiredToken));

        // 실행, 검증
        assertThatThrownBy(() -> refreshTokenUseCase.refresh(rawRefreshToken))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException custom = (CustomException) ex;
                    assertThat(custom.getErrorCode()).isEqualTo(ErrorCode.TOKEN_EXPIRED);
                });

        assertThat(expiredToken.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("회원 상태가 ACTIVE가 아니면 FORBIDDEN 예외 발생")
    void throwsWhenMemberNotActive() {
        // 준비
        String rawRefreshToken = "raw-refresh-token";
        Long memberId = 1L;

        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(memberId)
                .tokenHash("hash")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        Member inactiveMember = Member.builder()
                .id(memberId)
                .email("test@holliverse.com")
                .name("tester")
                .role(MemberRole.CUSTOMER)
                .status(MemberStatus.BANNED)
                .build();

        when(jwtTokenProvider.isValid(rawRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(rawRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getMemberId(rawRefreshToken)).thenReturn(memberId);
        when(refreshTokenHashService.hash(rawRefreshToken)).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse("hash"))
                .thenReturn(Optional.of(refreshToken));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(inactiveMember));

        // 실행, 검증
        assertThatThrownBy(() -> refreshTokenUseCase.refresh(rawRefreshToken))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException custom = (CustomException) ex;
                    assertThat(custom.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                });

        verify(memberRepository).findById(memberId);
    }
}
