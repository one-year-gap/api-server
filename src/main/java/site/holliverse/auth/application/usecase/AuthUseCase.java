package site.holliverse.auth.application.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.auth.dto.SignUpRequest;
import site.holliverse.auth.dto.SingUpResponse;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Address;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.entity.enums.MemberRoleType;
import site.holliverse.shared.persistence.entity.enums.MemberSignupType;
import site.holliverse.shared.persistence.entity.enums.MemberStatusType;
import site.holliverse.shared.persistence.repository.AddressRepository;
import site.holliverse.shared.persistence.repository.MemberRepository;

@Service
public class AuthUseCase {

    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthUseCase(
            MemberRepository memberRepository,
            AddressRepository addressRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SingUpResponse signUp(SignUpRequest request) {
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

        SignUpRequest.AddressRequest addressRequest = request.getAddress();
        Address address = addressRepository
                .findByProvinceAndCityAndStreetAddress(
                        addressRequest.getProvince(),
                        addressRequest.getCity(),
                        addressRequest.getStreetAddress()
                )
                .orElseGet(() -> addressRepository.save(new Address(
                        addressRequest.getProvince(),
                        addressRequest.getCity(),
                        addressRequest.getStreetAddress(),
                        addressRequest.getPostalCode()
                )));

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
                .status(MemberStatusType.ACTIVE)
                .role(MemberRoleType.CUSTOMER)
                .build();

        Member saved = memberRepository.save(member);
        return new SingUpResponse(saved.getId());
    }
}
