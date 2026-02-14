package site.holliverse.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import site.holliverse.shared.persistence.entity.enums.MemberMembershipType;

import java.time.LocalDate;

@Getter
public class SignUpRequest {

    @NotBlank @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 64)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    private String phone;

    @NotNull
    private LocalDate birthDate;

    // 'M' or 'F'
    @NotBlank
    @Pattern(regexp = "M|F")
    private String gender;

    @NotNull
    private MemberMembershipType membership;

    @Valid
    @NotNull
    private AddressRequest address;

    public static class AddressRequest {
        @NotBlank @Size(max = 50)
        private String province;

        @NotBlank @Size(max = 50)
        private String city;

        @NotBlank @Size(max = 100)
        private String streetAddress;

        @NotBlank @Size(max = 10)
        private String postalCode;

        public String getProvince() { return province; }
        public String getCity() { return city; }
        public String getStreetAddress() { return streetAddress; }
        public String getPostalCode() { return postalCode; }
    }
}
