package site.holliverse.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequest {

    // 로그인 아이디(이메일)
    @NotBlank
    @Email
    private String email;

    // 로그인 비밀번호
    @NotBlank
    @Size(min = 8, max = 64)
    private String password;
}
