package site.holliverse.auth.application.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.auth.dto.OnboardingCompleteRequestDto;
import site.holliverse.auth.dto.OnboardingPrefillResponseDto;
import site.holliverse.auth.dto.SignUpRequestDto;
import site.holliverse.auth.dto.SignUpResponseDto;
import site.holliverse.auth.jwt.RefreshTokenHashService;
import site.holliverse.shared.domain.model.MemberMembership;
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
import site.holliverse.shared.util.EncryptionTool;

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
    /** 개인정보(이름, 폰번호) 암호화를 위한 도구. */
    private final EncryptionTool encryptionTool;

    public AuthUseCase(
            MemberRepository memberRepository,
            AddressRepository addressRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenHashService refreshTokenHashService,
            PasswordEncoder passwordEncoder,
            EncryptionTool encryptionTool
    ) {
        this.memberRepository = memberRepository;
        this.addressRepository = addressRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenHashService = refreshTokenHashService;
        this.passwordEncoder = passwordEncoder;
        this.encryptionTool = encryptionTool;
    }

    /**
     * FORM 회원가입을 처리한다
     * 처리 순서:
     * 1) 개인정보 암호화 (검색 및 저장을 위해 선행)
     * 2) 이메일/전화번호 중복 검증 (암호화된 값으로 비교)
     * 3) 주소 재사용 또는 신규 생성
     * 4) 기본 권한/상태/가입유형으로 회원 저장
     */
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto request) {
        // DB 조회 및 저장을 위해 먼저 암호화를 수행
        String encryptedName = encryptionTool.encrypt(request.getName());
        String encryptedPhone = encryptionTool.encrypt(request.getPhone());

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(
                    ErrorCode.DUPLICATED_EMAIL,
                    "email"
            );
        }
        if (memberRepository.existsByPhone(encryptedPhone)) {
            throw new CustomException(
                    ErrorCode.DUPLICATED_PHONE,
                    "phone"
            );
        }


        /**
         * 주소를 찾고 주소 없으면 추가
         */
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

        /**
         * 회원 가입
         */

        Member member = Member.builder()
                .address(address)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(encryptedName)
                .phone(encryptedPhone)
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .membership(MemberMembership.GOLD)
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


    /**
     * 구글 처음 로그인 할시 추가 회원정보에 이메일과 이름은 띄워주기 위한 메서드
     */
    @Transactional(readOnly = true)
    public OnboardingPrefillResponseDto getOnboardingPrefill(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new CustomException(ErrorCode.MEMBER_NOT_FOUND,"memberId"));
        return new OnboardingPrefillResponseDto(member.getEmail(),member.getName());
    }

    /**
     * 구글 나머지 정보 입력 받는 메서드
     */

    @Transactional
    public void completeOnboarding(Long memberId, OnboardingCompleteRequestDto request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "memberId"));

        if (member.getStatus() != MemberStatus.PROCESSING) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "memberStatus");
        }

        String encryptedPhone = encryptionTool.encrypt(request.phone());
        if (memberRepository.existsByPhone(encryptedPhone)) {
            throw new CustomException(
                    ErrorCode.DUPLICATED_PHONE,
                    "phone"
            );
        }

        // request.address()에서 온보딩 요청의 주소 객체를 꺼낸다음 주소가 있으면 사용하고 없으면 새로운 주소 만들어서 쓴다.

        OnboardingCompleteRequestDto.AddressRequest addressRequest = request.address();
        Address address = addressRepository.findByProvinceAndCityAndStreetAddress(
                addressRequest.province(),
                addressRequest.city(),
                addressRequest.streetAddress()
        ).orElseGet(() -> addressRepository.save(Address.builder()
                .province(addressRequest.province())
                .city(addressRequest.city())
                .streetAddress(addressRequest.streetAddress())
                .postalCode(addressRequest.postalCode())
                .build()));

        member.completeOnboarding(
                address,
                encryptedPhone,
                request.birthDate(),
                request.gender(),
                request.membership()
        );
    }
}
