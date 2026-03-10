package site.holliverse.customer.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.customer.application.usecase.persona.GetMyPersonaUseCase;
import site.holliverse.customer.application.usecase.persona.PersonaDetailResult;
import site.holliverse.shared.config.web.GlobalExceptionHandler;
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.security.CustomUserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PersonaController 웹 레이어 테스트.
 *
 * 확인 포인트:
 * - 인증이 있으면 200 + 응답 바디 구조 검증
 * - 인증이 없으면 401 검증
 */
@WebMvcTest(controllers = PersonaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("customer")
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetMyPersonaUseCase getMyPersonaUseCase;

    // SecurityConfig 로딩 시 필요한 빈만 목 처리
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void clearSecurityContext() {
        // 테스트 간 인증 정보가 섞이지 않도록 항상 초기화
        SecurityContextHolder.clearContext();
    }

    /**
     * 테스트용 인증 사용자 생성.
     */
    private static CustomUserDetails principal(Long memberId) {
        return new CustomUserDetails(memberId, "user@test.com", null, "CUSTOMER", MemberStatus.ACTIVE);
    }

    /**
     * addFilters=false 환경에서 @AuthenticationPrincipal 주입을 받기 위해
     * SecurityContext를 수동으로 세팅한다.
     */
    private static void setAuthentication(CustomUserDetails principal) {
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    /**
     * 컨트롤러 응답 검증용 UseCase 결과 더미.
     */
    private static PersonaDetailResult result() {
        return new PersonaDetailResult(
                1L,
                "SPACE_EXPLORER",
                "short desc",
                "character desc",
                1,
                true,
                List.of("starter", "safe"),
                new PersonaDetailResult.TscoreIndex(
                        LocalDate.of(2026, 3, 10),
                        BigDecimal.valueOf(50),
                        BigDecimal.valueOf(51),
                        BigDecimal.valueOf(52),
                        BigDecimal.valueOf(53),
                        BigDecimal.valueOf(54),
                        BigDecimal.valueOf(55)
                ),
                false
        );
    }

    @Nested
    @DisplayName("GET /api/v1/customer/persona-types/me")
    class GetMyPersona {

        @Test
        @DisplayName("authenticated request returns 200 with persona payload")
        void authenticated_returns200() throws Exception {
            // given: 인증 사용자 + 유스케이스 반환값 준비
            setAuthentication(principal(1006L));
            given(getMyPersonaUseCase.execute(1006L)).willReturn(result());

            // when/then: 200과 주요 응답 필드를 확인
            mockMvc.perform(get("/api/v1/customer/persona-types/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.personaTypeId").value(1))
                    .andExpect(jsonPath("$.data.characterName").value("SPACE_EXPLORER"))
                    .andExpect(jsonPath("$.data.tags[0]").value("starter"))
                    .andExpect(jsonPath("$.data.tscoreIndex.exploreTscore").value(50))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("unauthenticated request returns 401")
        void unauthenticated_returns401() throws Exception {
            // given: 인증 정보 없음
            // when/then: 컨트롤러 null 가드에 의해 401
            mockMvc.perform(get("/api/v1/customer/persona-types/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
