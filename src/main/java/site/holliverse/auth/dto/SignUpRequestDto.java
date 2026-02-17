package site.holliverse.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import site.holliverse.shared.domain.model.MemberMembership;

import java.time.LocalDate;

/**
 * 회원가입 요청 JSON DTO.
 */
@Getter
public class SignUpRequestDto {

    /** 로그인 식별자로 사용할 고유 이메일. */
    @NotBlank(message = "필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    /** 저장 전 인코딩되는 원문 비밀번호. */
    @NotBlank(message = "필수 입력값입니다.")
    @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하로 입력해주세요.")
    private String password;

    /** 사용자 이름. */
    @NotBlank(message = "필수 입력값입니다.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    private String name;

    /** 숫자만 허용되는 전화번호. */
    @NotBlank(message = "필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]+$", message = "숫자만 입력 가능합니다.")
    private String phone;

    /** 생년월일. */
    @NotNull(message = "필수 입력값입니다.")
    private LocalDate birthDate;

    /** 성별 코드(M/F). */
    @NotBlank(message = "필수 입력값입니다.")
    @Pattern(regexp = "M|F", message = "성별은 M 또는 F만 입력 가능합니다.")
    private String gender;

    /** 멤버십 등급. */
    @NotNull(message = "필수 입력값입니다.")
    private MemberMembership membership;

    /** 중첩 주소 정보(@Valid 재귀 검증). */
    @Valid
    @NotNull(message = "필수 입력값입니다.")
    private AddressRequest address;

    /**
     * 회원가입 주소 하위 DTO.
     */
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