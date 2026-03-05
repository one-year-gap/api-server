package site.holliverse.customer.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.customer.application.usecase.member.GetCustomerProfileUseCase;
import site.holliverse.customer.application.usecase.member.GetRecentActivitiesUseCase;
import site.holliverse.customer.application.usecase.member.RecentActivityResult;
import site.holliverse.customer.web.dto.member.RecentActivityResponse;
import site.holliverse.customer.web.mapper.CustomerProfileResponseMapper;
import site.holliverse.customer.web.mapper.RecentActivityResponseMapper;
import site.holliverse.shared.security.CustomUserDetails;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MemberController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
@ActiveProfiles("customer")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCustomerProfileUseCase getCustomerProfileUseCase;

    @MockitoBean
    private CustomerProfileResponseMapper customerProfileResponseMapper;

    @MockitoBean
    private GetRecentActivitiesUseCase getRecentActivitiesUseCase;

    @MockitoBean
    private RecentActivityResponseMapper recentActivityResponseMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private Authentication authenticationWithMemberId(Long memberId) {
        CustomUserDetails user = mock(CustomUserDetails.class);
        given(user.getMemberId()).willReturn(memberId);
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/customer/recent-activities")
    class GetRecentActivities {

        @Test
        @DisplayName("limit와 인증 사용자 memberId로 최근 활동을 조회하고 ApiResponse 구조로 응답한다")
        void success_returnsRecentActivities() throws Exception {
            Long memberId = 10L;
            int limit = 3;

            OffsetDateTime now = OffsetDateTime.of(2026, 3, 2, 16, 30, 0, 0, ZoneOffset.UTC);
            RecentActivityResult result = new RecentActivityResult(
                    List.of(
                            new RecentActivityResult.ActivityItem(
                                    10L,
                                    "5G 요금제",
                                    "mobile",
                                    List.of("영상OTT", "인기"),
                                    now
                            ),
                            new RecentActivityResult.ActivityItem(
                                    8L,
                                    "기가 인터넷",
                                    "internet",
                                    List.of("속도"),
                                    now.minusDays(1)
                            )
                    )
            );

            RecentActivityResponse response = new RecentActivityResponse(
                    List.of(
                            new RecentActivityResponse.ActivityItem(
                                    10L,
                                    "5G 요금제",
                                    "mobile",
                                    List.of("영상OTT", "인기"),
                                    now
                            ),
                            new RecentActivityResponse.ActivityItem(
                                    8L,
                                    "기가 인터넷",
                                    "internet",
                                    List.of("속도"),
                                    now.minusDays(1)
                            )
                    )
            );

            given(getRecentActivitiesUseCase.execute(memberId, limit)).willReturn(result);
            given(recentActivityResponseMapper.toResponse(result)).willReturn(response);

            mockMvc.perform(get("/api/v1/customer/recent-activities")
                            .param("limit", "3")
                            .with(authentication(authenticationWithMemberId(memberId)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.items[0].productId").value(10))
                    .andExpect(jsonPath("$.data.items[0].productName").value("5G 요금제"))
                    .andExpect(jsonPath("$.data.items[0].productType").value("mobile"))
                    .andExpect(jsonPath("$.data.items[0].tags[0]").value("영상OTT"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(getRecentActivitiesUseCase).execute(eq(memberId), eq(limit));
            verify(recentActivityResponseMapper).toResponse(result);
        }

    }
}

