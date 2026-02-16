package site.holliverse.auth.application.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.auth.dto.SignUpRequestDto;
import site.holliverse.auth.dto.SignUpResponseDto;
import site.holliverse.auth.jwt.RefreshTokenHashService;
import site.holliverse.shared.domain.model.MemberRole;
import site.holliverse.shared.domain.model.MemberSignupType;
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Address;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.AddressRepository;
import site.holliverse.shared.persistence.repository.MemberRepository;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;

/**
 * 인증 도메인의 계정 생명주기 유스케이스.
 * 현재 담당 기능:
 * - 회원가입(중복 검증 + 비밀번호 해시 저장)
 * - 로그아웃(리프레시 토큰 폐기)
 */
@Service
public class AuthUseCase {

    /** 회원 엔티티 조회/저장 리포지토리. */
    private final MemberRepository memberRepository;
    /** 주소 중복 재사용을 위한 주소 리포지토리. */
    private final AddressRepository addressRepository;
    /** 리프레시 토큰 해시/폐기 상태 저장 리포지토리. */
    private final RefreshTokenRepository refreshTokenRepository;
    /** 원문 리프레시 토큰을 해시로 변환하는 유틸. */
    private final RefreshTokenHashService refreshTokenHashService;
    /** 원문 비밀번호를 안전하게 인코딩하는 컴포넌트. */
    private final PasswordEncoder passwordEncoder;

    public AuthUseCase(
            MemberRepository memberRepository,
            AddressRepository addressRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenHashService refreshTokenHashService,
            PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.addressRepository = addressRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenHashService = refreshTokenHashService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * FORM 회원가입을 처리한다
     * 처리 순서:
     * 1) 이메일/전화번호 중복 검증
     * 2) 주소 재사용 또는 신규 생성
     * 3) 기본 권한/상태/가입유형으로 회원 저장
     */
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(
                    ErrorCode.DUPLICATED_EMAIL,
                    "email",
                    "동일한 이메일이 존재합니다."
            );
        }
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new CustomException(
                    ErrorCode.DUPLICATED_PHONE,
                    "phone",
                    "동일한 전화번호가 존재합니다."
            );
        }

        SignUpRequestDto.AddressRequest addressRequest = request.getAddress();
        Address address = addressRepository
                .findByProvinceAndCityAndStreetAddress(
                        addressRequest.getProvince(),
                        addressRequest.getCity(),
                        addressRequest.getStreetAddress()
                )
                .orElseGet(() -> addressRepository.save(
                        Address.builder()
                                .province(addressRequest.getProvince())
                                .city(addressRequest.getCity())
                                .streetAddress(addressRequest.getStreetAddress())
                                .postalCode(addressRequest.getPostalCode())
                                .build()
                ));

        Member member = Member.builder()
                .address(address)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .membership(request.getMembership())
                .type(MemberSignupType.FORM)
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.CUSTOMER)
                .build();

        Member saved = memberRepository.save(member);
        return new SignUpResponseDto(saved.getId());
    }

    /**
     * 로그아웃 시 리프레시 토큰을 폐기한다.
     * - 토큰이 없거나 비어 있으면 그대로 종료
     * - 이미 폐기/미존재 토큰도 실패 없이 종료
     */
    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        String tokenHash = refreshTokenHashService.hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(refreshToken -> refreshToken.revoke());
    }
}
