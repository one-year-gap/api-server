package site.holliverse.customer.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.customer.application.usecase.product.ChangeProductResult;
import site.holliverse.customer.application.usecase.product.ChangeProductUseCase;
import site.holliverse.customer.web.assembler.ChangeProductResponseAssembler;
import site.holliverse.customer.web.dto.product.change.ChangeProductRequest;
import site.holliverse.customer.web.dto.product.change.ChangeProductResponse;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = SubscriptionController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
@ActiveProfiles("customer")
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChangeProductUseCase changeProductUseCase;

    @MockitoBean
    private ChangeProductResponseAssembler changeProductResponseAssembler;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final Long MEMBER_ID = 1L;
    private static final Long TARGET_PRODUCT_ID = 5L;

    @Nested
    @DisplayName("POST /api/v1/plans/change 요금제 변경 / 신청")
    class ChangePlan {

        @Test
        @DisplayName("성공: memberId, targetProductId 전달 시 200과 ApiResponse 구조로 응답")
        void success_returns200WithResponse() throws Exception {
            // given: ChangeProductResponse는 subscription_id, product_id, product_name, sale_price, start_date 로 직렬화됨
            ChangeProductResult useCaseResult = new ChangeProductResult(
                    5002L,
                    101L,
                    "5G 시그니처",
                    97500,        // salePrice
                    LocalDateTime.of(2026, 2, 13, 11, 15)  // startDate
            );
            ChangeProductResponse response = new ChangeProductResponse(
                    5002L,
                    101L,
                    "5G 시그니처",
                    97500,
                    LocalDateTime.of(2026, 2, 13, 11, 15)
            );
            ChangeProductRequest request = new ChangeProductRequest(MEMBER_ID, TARGET_PRODUCT_ID);

            given(changeProductUseCase.execute(MEMBER_ID, TARGET_PRODUCT_ID)).willReturn(useCaseResult);
            given(changeProductResponseAssembler.assemble(useCaseResult)).willReturn(response);

            // when & then: API 응답은 sale_price, start_date 필드로 내려감
            mockMvc.perform(post("/api/v1/plans/change")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.subscription_id").value(5002))
                    .andExpect(jsonPath("$.data.product_id").value(101))
                    .andExpect(jsonPath("$.data.product_name").value("5G 시그니처"))
                    .andExpect(jsonPath("$.data.sale_price").value(97500))
                    .andExpect(jsonPath("$.data.start_date").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andDo(print());

            verify(changeProductUseCase).execute(MEMBER_ID, TARGET_PRODUCT_ID);
            verify(changeProductResponseAssembler).assemble(useCaseResult);
        }

        @Test
        @DisplayName("요청 body에 memberId, targetProductId가 포함되어 UseCase에 전달된다")
        void requestBody_mapsToUseCaseArgs() throws Exception {
            ChangeProductRequest request = new ChangeProductRequest(2L, 10L);
            ChangeProductResult useCaseResult = new ChangeProductResult(
                    200L, 10L, "상품명", 9000, LocalDateTime.now());
            given(changeProductUseCase.execute(2L, 10L)).willReturn(useCaseResult);
            given(changeProductResponseAssembler.assemble(useCaseResult))
                    .willReturn(new ChangeProductResponse(200L, 10L, "상품명", 9000, LocalDateTime.now()));

            mockMvc.perform(post("/api/v1/plans/change")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(changeProductUseCase).execute(2L, 10L);
        }
    }
}
