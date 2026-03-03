package site.holliverse.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record OnboardingCompleteRequestDto(
        @NotBlank(message = "필수 입력값입니다.")
        @Pattern(regexp = "^[0-9]+$", message = "숫자만 입력 가능합니다.")
        String phone,

        @NotNull(message = "필수 입력값입니다.")
        LocalDate birthDate,

        @NotBlank(message = "필수 입력값입니다.")
        @Pattern(regexp = "M|F", message = "성별은 M 또는 F만 입력 가능합니다.")
        String gender,

        @Valid
        @NotNull(message = "필수 입력값입니다.")
        AddressRequest address
) {
    public record AddressRequest(
            @NotBlank(message = "필수 입력값입니다.")
            @Size(max = 50, message = "province는 50자 이하로 입력해주세요.")
            String province,

            @NotBlank(message = "필수 입력값입니다.")
            @Size(max = 50, message = "city는 50자 이하로 입력해주세요.")
            String city,

            @NotBlank(message = "필수 입력값입니다.")
            @Size(max = 100, message = "streetAddress는 100자 이하로 입력해주세요.")
            String streetAddress,

            @NotBlank(message = "필수 입력값입니다.")
            @Size(max = 10, message = "postalCode는 10자 이하로 입력해주세요.")
            String postalCode
    ) {
    }
}
