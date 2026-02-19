package site.holliverse.auth.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
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
import site.holliverse.shared.persistence.entity.RefreshToken;
import site.holliverse.shared.persistence.repository.AddressRepository;
import site.holliverse.shared.persistence.repository.MemberRepository;
import site.holliverse.shared.persistence.repository.RefreshTokenRepository;
import site.holliverse.shared.util.EncryptionTool;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenHashService refreshTokenHashService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EncryptionTool encryptionTool;

    @InjectMocks
    private AuthUseCase authUseCase;

    @Nested
    @DisplayName("signUp")
    class SignUp {

        @Test
        @DisplayName("기존 주소가 있으면 재사용하여 회원가입한다")
        void signUpSuccessWithExistingAddress() {
            // given
            SignUpRequestDto request = createRequest();
            Address existingAddress = Address.builder()
                    .province("seoul")
                    .city("gangnam")
                    .streetAddress("teheran-ro 123")
                    .postalCode("06234")
                    .build();

            // 암호화 동작 정의 (입력값 -> 암호문 반환)
            when(encryptionTool.encrypt(request.getName())).thenReturn("encrypted-name");
            when(encryptionTool.encrypt(request.getPhone())).thenReturn("encrypted-phone");

            when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(memberRepository.existsByPhone("encrypted-phone")).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
            when(addressRepository.findByProvinceAndCityAndStreetAddress(
                    eq("seoul"), eq("gangnam"), eq("teheran-ro 123")
            )).thenReturn(Optional.of(existingAddress));
            when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
                Member member = invocation.getArgument(0);
                ReflectionTestUtils.setField(member, "id", 1L);
                return member;
            });

            // when
            SignUpResponseDto result = authUseCase.signUp(request);

            // then
            assertThat(result.memberId()).isEqualTo(1L);
            verify(addressRepository, never()).save(any(Address.class));

            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository).save(memberCaptor.capture());
            Member savedMember = memberCaptor.getValue();

            assertThat(savedMember.getAddress()).isEqualTo(existingAddress);
            assertThat(savedMember.getPassword()).isEqualTo("encoded-password");

            // DB에 저장된 값이 암호문인지 확인
            assertThat(savedMember.getName()).isEqualTo("encrypted-name");
            assertThat(savedMember.getPhone()).isEqualTo("encrypted-phone");

            assertThat(savedMember.getRole()).isEqualTo(MemberRole.CUSTOMER);
            assertThat(savedMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(savedMember.getType()).isEqualTo(MemberSignupType.FORM);
            assertThat(savedMember.getMembership()).isEqualTo(MemberMembership.BASIC);
        }

        @Test
        @DisplayName("주소가 없으면 새 주소를 저장한 뒤 회원가입한다")
        void signUpSuccessWithNewAddress() {
            // given
            SignUpRequestDto request = createRequest();
            when(encryptionTool.encrypt(request.getName())).thenReturn("encrypted-name");
            when(encryptionTool.encrypt(request.getPhone())).thenReturn("encrypted-phone");
            when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(memberRepository.existsByPhone("encrypted-phone")).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
            when(addressRepository.findByProvinceAndCityAndStreetAddress(
                    any(), any(), any()
            )).thenReturn(Optional.empty());
            when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
                Member member = invocation.getArgument(0);
                ReflectionTestUtils.setField(member, "id", 2L);
                return member;
            });

            // when
            SignUpResponseDto result = authUseCase.signUp(request);

            // then
            assertThat(result.memberId()).isEqualTo(2L);

            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(addressCaptor.capture());
            Address savedAddress = addressCaptor.getValue();
            assertThat(savedAddress.getProvince()).isEqualTo("seoul");
            assertThat(savedAddress.getCity()).isEqualTo("gangnam");
            assertThat(savedAddress.getStreetAddress()).isEqualTo("teheran-ro 123");
            assertThat(savedAddress.getPostalCode()).isEqualTo("06234");

            // [추가 검증] 저장된 멤버의 정보가 암호화되었는지 확인
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository).save(memberCaptor.capture());
            Member savedMember = memberCaptor.getValue();
            assertThat(savedMember.getName()).isEqualTo("encrypted-name");
            assertThat(savedMember.getPhone()).isEqualTo("encrypted-phone");
        }

        @Test
        @DisplayName("이메일이 중복이면 예외를 던진다")
        void throwsWhenEmailDuplicated() {
            // given
            SignUpRequestDto request = createRequest();
            when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);

            // when, then
            assertThatThrownBy(() -> authUseCase.signUp(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException custom = (CustomException) ex;
                        assertThat(custom.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_EMAIL);
                        assertThat(custom.getField()).isEqualTo("email");
                    });
        }

        @Test
        @DisplayName("전화번호가 중복이면 예외를 던진다")
        void throwsWhenPhoneDuplicated() {
            // given
            SignUpRequestDto request = createRequest();

            when(encryptionTool.encrypt(request.getName())).thenReturn("encrypted-name");
            when(encryptionTool.encrypt(request.getPhone())).thenReturn("encrypted-phone");

            when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(memberRepository.existsByPhone("encrypted-phone")).thenReturn(true);

            // when, then
            assertThatThrownBy(() -> authUseCase.signUp(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException custom = (CustomException) ex;
                        assertThat(custom.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_PHONE);
                        assertThat(custom.getField()).isEqualTo("phone");
                    });
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("토큰이 비어있으면 아무 동작도 하지 않는다")
        void noOpWhenTokenIsBlank() {
            // when
            authUseCase.logout(" ");

            // then
            verifyNoInteractions(refreshTokenHashService, refreshTokenRepository);
        }

        @Test
        @DisplayName("토큰이 있으면 해시 조회 후 토큰을 폐기한다")
        void revokeWhenTokenExists() {
            // given
            RefreshToken refreshToken = RefreshToken.builder()
                    .memberId(1L)
                    .tokenHash("hash-value")
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .revoked(false)
                    .build();
            when(refreshTokenHashService.hash("raw-token")).thenReturn("hash-value");
            when(refreshTokenRepository.findByTokenHashAndRevokedFalse("hash-value"))
                    .thenReturn(Optional.of(refreshToken));

            // when
            authUseCase.logout("raw-token");

            // then
            assertThat(refreshToken.isRevoked()).isTrue();
        }
    }

    private SignUpRequestDto createRequest() {
        SignUpRequestDto request = new SignUpRequestDto();
        ReflectionTestUtils.setField(request, "email", "test@holliverse.com");
        ReflectionTestUtils.setField(request, "password", "Password!123");
        ReflectionTestUtils.setField(request, "name", "hong");
        ReflectionTestUtils.setField(request, "phone", "01012345678");
        ReflectionTestUtils.setField(request, "birthDate", LocalDate.of(1999, 1, 1));
        ReflectionTestUtils.setField(request, "gender", "M");
        ReflectionTestUtils.setField(request, "membership", MemberMembership.BASIC);

        SignUpRequestDto.AddressRequest address = new SignUpRequestDto.AddressRequest();
        ReflectionTestUtils.setField(address, "province", "seoul");
        ReflectionTestUtils.setField(address, "city", "gangnam");
        ReflectionTestUtils.setField(address, "streetAddress", "teheran-ro 123");
        ReflectionTestUtils.setField(address, "postalCode", "06234");
        ReflectionTestUtils.setField(request, "address", address);
        return request;
    }
}
