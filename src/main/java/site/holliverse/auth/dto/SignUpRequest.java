package site.holliverse.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import site.holliverse.shared.persistence.entity.enums.MemberMembershipType;

import java.time.LocalDate;

@Getter
// 회원가입 요청 DTO
public class SignUpRequest {

    @NotBlank(message = "필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "필수 입력값입니다.")
    @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "필수 입력값입니다.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]+$", message = "숫자만 입력 가능합니다.")
    private String phone;

    @NotNull(message = "필수 입력값입니다.")
    private LocalDate birthDate;

    @NotBlank(message = "필수 입력값입니다.")
    @Pattern(regexp = "M|F", message = "성별은 M 또는 F만 입력 가능합니다.")
    private String gender;

    @NotNull(message = "필수 입력값입니다.")
    private MemberMembershipType membership;

    @Valid
    @NotNull(message = "필수 입력값입니다.")
    private AddressRequest address;

    @Getter
    public static class AddressRequest {
        @NotBlank(message = "필수 입력값입니다.")
        @Size(max = 50, message = "province는 50자 이하로 입력해주세요.")
        private String province;

        @NotBlank(message = "필수 입력값입니다.")
        @Size(max = 50, message = "city는 50자 이하로 입력해주세요.")
        private String city;

        @NotBlank(message = "필수 입력값입니다.")
        @Size(max = 100, message = "streetAddress는 100자 이하로 입력해주세요.")
        private String streetAddress;

        @NotBlank(message = "필수 입력값입니다.")
        @Size(max = 10, message = "postalCode는 10자 이하로 입력해주세요.")
        private String postalCode;
    }
}
