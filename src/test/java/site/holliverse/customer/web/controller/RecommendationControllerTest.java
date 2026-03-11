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
import site.holliverse.customer.application.usecase.recommendation.RecommendationResult;
import site.holliverse.customer.application.usecase.recommendation.RecommendationService;
import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.customer.web.CustomerWebConfig;
import site.holliverse.shared.config.web.GlobalExceptionHandler;
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.domain.model.PersonaSegment;
import site.holliverse.shared.security.CustomUserDetails;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CustomerWebConfig.class})
@ActiveProfiles("customer")
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static CustomUserDetails testPrincipal() {
        return new CustomUserDetails(1L, "user@test.com", null, "CUSTOMER", MemberStatus.ACTIVE);
    }

    private static void setAuthentication(CustomUserDetails principal) {
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    private static RecommendationResult result(RecommendationResult.RecommendationSource source) {
        return new RecommendationResult(
                PersonaSegment.UPSELL,
                "캐시 문구",
                List.of(new RecommendedProductItem(1L, "추천 이유")),
                source,
                Instant.now()
        );
    }

    @Nested
    @DisplayName("GET /api/v1/customer/recommendations")
    class GetRecommendations {

        @Test
        @DisplayName("인증 시 200, 응답에 segment·source·recommendedProducts 포함")
        void authenticated_returns200_withBody() throws Exception {
            setAuthentication(testPrincipal());
            given(recommendationService.getRecommendations(1L)).willReturn(result(RecommendationResult.RecommendationSource.CACHE));

            mockMvc.perform(get("/api/v1/customer/recommendations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.segment").value("UPSELL"))
                    .andExpect(jsonPath("$.data.source").value("CACHE"))
                    .andExpect(jsonPath("$.data.recommendedProducts").isArray())
                    .andExpect(jsonPath("$.data.recommendedProducts[0].productId").value(1))
                    .andExpect(jsonPath("$.data.recommendedProducts[0].reason").value("추천 이유"));
        }

        @Test
        @DisplayName("미인증 시 401")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/customer/recommendations"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
