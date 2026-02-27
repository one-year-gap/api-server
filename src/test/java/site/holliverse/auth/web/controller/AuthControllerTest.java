package site.holliverse.auth.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.auth.application.usecase.AuthUseCase;
import site.holliverse.auth.application.usecase.RefreshTokenUseCase;
import site.holliverse.auth.dto.SignUpResponseDto;
import site.holliverse.auth.dto.TokenRefreshResponseDto;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.shared.config.web.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원가입 성공 시 201과 성공 응답을 반환한다")
    void signUpSuccess() throws Exception {
        // given
        BDDMockito.given(authUseCase.signUp(any())).willReturn(new SignUpResponseDto(1L));
        String requestBody = """
                {
                  "email": "test@holliverse.com",
                  "password": "Password!123",
                  "name": "hong",
                  "phone": "01012345678",
                  "birthDate": "1999-01-01",
                  "gender": "M",
                  "address": {
                    "province": "seoul",
                    "city": "gangnam",
                    "streetAddress": "teheran-ro 123",
                    "postalCode": "06234"
                  }
                }
                """;

        // when, then
        mockMvc.perform(post("/api/v1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.memberId").value(1L));
    }

    @Test
    @DisplayName("리프레시 쿠키가 없으면 401을 반환한다")
    void refreshWithoutCookie() throws Exception {
        // when, then
        mockMvc.perform(post("/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorDetail.code").value("REFRESH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("리프레시 쿠키가 있으면 토큰 재발급 성공 응답을 반환한다")
    void refreshSuccess() throws Exception {
        // given
        BDDMockito.given(refreshTokenUseCase.refresh(eq("refresh-token-value")))
                .willReturn(new TokenRefreshResponseDto(
                        "new-access-token",
                        3600L,
                        "new-refresh-token",
                        1209600L
                ));

        // when, then
        mockMvc.perform(post("/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token-value")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @DisplayName("로그아웃 요청 시 토큰을 만료시키고 쿠키를 제거한다")
    void logoutSuccess() throws Exception {
        // when, then
        mockMvc.perform(post("/v1/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token-value")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(cookie().maxAge("refreshToken", 0));

        then(authUseCase).should().logout("refresh-token-value");
    }
}
