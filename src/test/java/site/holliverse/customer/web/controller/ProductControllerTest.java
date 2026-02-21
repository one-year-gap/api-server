package site.holliverse.customer.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.customer.application.usecase.compare.ComparisonResultDto;
import site.holliverse.customer.application.usecase.compare.PlanComparator;
import site.holliverse.customer.application.usecase.compare.PlanComparatorTestData;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import site.holliverse.customer.application.usecase.product.GetProductDetailUseCase;
import site.holliverse.customer.application.usecase.product.GetProductListUseCase;
import site.holliverse.customer.application.usecase.product.ProductDetailResult;
import site.holliverse.customer.application.usecase.product.ProductListResult;
import site.holliverse.customer.web.assembler.PlanCompareResponseAssembler;
import site.holliverse.customer.web.assembler.ProductListResponseAssembler;
import site.holliverse.customer.web.dto.PageMeta;
import site.holliverse.customer.web.dto.product.ProductDetailResponse;
import site.holliverse.customer.web.dto.product.ProductListResponse;
import site.holliverse.customer.web.dto.product.compare.ComparisonResponse;
import site.holliverse.customer.web.dto.product.compare.PlanCompareResponse;
import site.holliverse.customer.web.dto.product.ProductContent;
import site.holliverse.customer.web.mapper.ProductResponseMapper;
import site.holliverse.shared.domain.model.ProductType;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProductController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
@ActiveProfiles("customer")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetProductListUseCase getProductListUseCase;

    @MockitoBean
    private GetProductDetailUseCase getProductDetailUseCase;
    @MockitoBean
    private PlanComparator planComparator;
    @MockitoBean
    private ProductListResponseAssembler productListResponseAssembler;
    @MockitoBean
    private PlanCompareResponseAssembler planCompareResponseAssembler;
    @MockitoBean
    private ProductResponseMapper mapper;
    /** SecurityConfig 등 로드 시 필요. JWT 로직은 사용하지 않고 컨텍스트 기동용 목만 둠. */
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    // --- Helper Methods ---
    private static ProductSummaryDto summaryDto(Long productId, String name, ProductType productType) {
        return new ProductSummaryDto(
                productId,
                name,
                10000,
                8000,
                productType,
                "CODE-001",
                "할인"
        );
    }

    private static ProductDetailResponse detailResponse(Long productId, String name, String productType) {
        return new ProductDetailResponse(
                productId,
                name,
                productType,
                10000,
                8000,
                "할인",
                "CODE-001",
                (ProductContent) null,
                false
        );
    }

    @Nested
    @DisplayName("GET /api/v1/customer/plans - 카테고리별 상품 목록")
    class GetPlanList {

        @Test
        @DisplayName("성공: 카테고리 지정시 페이징 데이터와 함께 200 ok를 반환한다")
        void whenCategoryGiven_returns200WithPageAndContent() throws Exception {
            //give

            ProductListResult result = new ProductListResult(
                    new PageImpl<>(List.of(summaryDto(1L, "상품1", ProductType.MOBILE_PLAN)), PageRequest.of(0, 20), 1),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
            ProductListResponse listResponse = new ProductListResponse(
                    new PageMeta(1L, 1, 0, 20),
                    List.of(detailResponse(1L, "상품1", "MOBILE_PLAN"))
            );
            given(getProductListUseCase.execute(eq("mobile"), anyInt(), anyInt(), anyInt())).willReturn(result);
            given(productListResponseAssembler.assemble(result)).willReturn(listResponse);


            //when & then
            mockMvc.perform(get("/api/v1/customer/plans").param("category", "mobile"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.page").exists())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.timestamp").exists());
            verify(getProductListUseCase).execute("mobile", 0, 20, 0);
        }

        @Test
        @DisplayName("정상 – page/size 지정 시 UseCase에 해당 값으로 호출됨")
        void whenPageAndSizeGiven_useCaseCalledWithThoseValues() throws Exception {
            ProductListResult result = new ProductListResult(
                    new PageImpl<>(List.of(), PageRequest.of(0, 10), 0),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
            ProductListResponse listResponse = new ProductListResponse(
                    new PageMeta(0L, 0, 0, 10),
                    List.of()
            );

            when(getProductListUseCase.execute(eq("internet"), eq(0), eq(10), eq(0))).thenReturn(result);
            when(productListResponseAssembler.assemble(result)).thenReturn(listResponse);

            mockMvc.perform(get("/api/v1/customer/plans")
                            .param("category", "internet")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"));

            verify(getProductListUseCase).execute("internet", 0, 10, 0);
            verify(productListResponseAssembler).assemble(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"iptv", "add-on", "tab-watch"})
        @DisplayName("성공: 다양한 카테고리에 대해 정상적으로 응답한다")
        void getPlans_VariousCategories(String category) throws Exception {
            //given

            given(getProductListUseCase.execute(eq(category),
                                                anyInt(),
                                                anyInt(),
                                                anyInt())).willReturn(new ProductListResult(new PageImpl<>(List.of()),
                                                                                            List.of(),
                                                                                            List.of(),
                                                                                            List.of(),
                                                                                            List.of(),
                                                                                            List.of(),
                                                                                            List.of()));
            given(productListResponseAssembler.assemble(any())).willReturn(new ProductListResponse(new PageMeta(0L,
                                                                                                                0,
                                                                                                                0,
                                                                                                                20),
                                                                                                   List.of()));

            //when & then
            mockMvc.perform(get("/api/v1/customer/plans").param("category", category))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"));
        }

        @Test
        @DisplayName("실패: 카테고리 파라미터 누락되면 400 Bad Request 반환")
        void whenCategoryMissing_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/customer/plans"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("정상: bestCount 파라미터 전달 시 UseCase에 해당 값으로 호출된다")
        void whenBestCountGiven_useCaseCalledWithBestCount() throws Exception {
            ProductListResult result = new ProductListResult(
                    new PageImpl<>(List.of(), PageRequest.of(0, 20), 0),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
            when(getProductListUseCase.execute(eq("mobile"), eq(0), eq(20), eq(5))).thenReturn(result);
            when(productListResponseAssembler.assemble(result)).thenReturn(new ProductListResponse(new PageMeta(0L, 0, 0, 20), List.of()));

            mockMvc.perform(get("/api/v1/customer/plans")
                            .param("category", "mobile")
                            .param("bestCount", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"));

            verify(getProductListUseCase).execute("mobile", 0, 20, 5);
        }

    }

    @Nested
    @DisplayName("GET /api/v1/customer/plans/{planId} - 특정 상품 스펙 조회")
    class GetPlanDetail {

        @Test
        @DisplayName("성공: 존재하는 planId 요청 시 200 ok와 productId·name·productType·data를 반환한다")
        void whenPlanIdExists_returns200WithProductDetail() throws Exception {
            //given
            Long planId = 1L;
            ProductDetailResult result = new ProductDetailResult(
                    summaryDto(planId, "5G 요금제", ProductType.MOBILE_PLAN),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
            ProductDetailResponse response = detailResponse(planId, "5G 요금제", "MOBILE_PLAN");
            given(getProductDetailUseCase.execute(planId)).willReturn(result);
            given(mapper.toDetailResponse(result)).willReturn(response);

            //when & then
            mockMvc.perform(get("/api/v1/customer/plans/{planId}", planId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.productId").value(1))
                    .andExpect(jsonPath("$.data.name").value("5G 요금제"))
                    .andExpect(jsonPath("$.data.productType").value("MOBILE_PLAN"))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
            verify(getProductDetailUseCase).execute(planId);
            verify(mapper).toDetailResponse(result);
        }

        @Test
        @DisplayName("성공: planId로 UseCase 호출 후 Mapper가 결과로 한 번 호출된다")
        void whenPlanId99_useCaseAndMapperCalledCorrectly() throws Exception {
            //given
            Long planId = 99L;
            ProductDetailResult result = new ProductDetailResult(
                    summaryDto(planId, "상품99", ProductType.INTERNET),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
            ProductDetailResponse response = detailResponse(planId, "상품99", "INTERNET");
            given(getProductDetailUseCase.execute(planId)).willReturn(result);
            given(mapper.toDetailResponse(result)).willReturn(response);

            //when & then
            mockMvc.perform(get("/api/v1/customer/plans/{planId}", planId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.productId").value(99));
            verify(getProductDetailUseCase).execute(planId);
            verify(mapper).toDetailResponse(result);
        }

        @Test
        @DisplayName("실패: planId가 숫자가 아니면 400 Bad Request 반환")
        void whenPlanIdNotNumeric_returns400() throws Exception {
            //when & then
            mockMvc.perform(get("/api/v1/customer/plans/abc"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customer/plans/compare - 요금제 비교")
    class ComparePlans {

        @Test
        @DisplayName("성공: 200과 비교 결과를 반환한다 (Web 계층 오케스트레이션, Assembler/Mapper 미의존)")
        void whenEssentialToPlus_returns200WithCompareResponse() throws Exception {
            // Controller가 GetProductDetailUseCase 2회 호출 → 검증 → PlanComparator → Assembler
            ProductDetailResult currentResult = new ProductDetailResult(
                    PlanComparatorTestData.essentialSummary(),
                    Optional.of(PlanComparatorTestData.essentialMobilePlan()),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );
            ProductDetailResult targetResult = new ProductDetailResult(
                    PlanComparatorTestData.plusSummary(),
                    Optional.of(PlanComparatorTestData.plusMobilePlan()),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );
            given(getProductDetailUseCase.execute(1L)).willReturn(currentResult);
            given(getProductDetailUseCase.execute(2L)).willReturn(targetResult);
            given(planComparator.compare(any(), any(), any(), any()))
                    .willReturn(new ComparisonResultDto(15_000, "+15,000원", List.of()));

            ProductDetailResponse currentPlanDto = new ProductDetailResponse(
                    1L, "5G 프리미어 에센셜", "MOBILE_PLAN", 59_000, 49_500, "할인", "CODE", null, false);
            ProductDetailResponse targetPlanDto = new ProductDetailResponse(
                    2L, "5G 프리미어 플러스", "MOBILE_PLAN", 74_000, 62_000, "할인", "CODE", null, false);
            PlanCompareResponse mockResponse = new PlanCompareResponse(
                    currentPlanDto, targetPlanDto,
                    new ComparisonResponse(15_000, "+15,000원", List.of())
            );
            given(planCompareResponseAssembler.assemble(any(ProductDetailResult.class), any(ProductDetailResult.class), any(ComparisonResultDto.class)))
                    .willReturn(mockResponse);

            mockMvc.perform(get("/api/v1/customer/plans/compare")
                            .param("currentPlanId", "1")
                            .param("targetPlanId", "2"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.current_plan.name").value("5G 프리미어 에센셜"))
                    .andExpect(jsonPath("$.data.target_plan.name").value("5G 프리미어 플러스"))
                    .andExpect(jsonPath("$.data.comparison.price_diff").value(15000))
                    .andExpect(jsonPath("$.data.comparison.message").value("+15,000원"))
                    .andExpect(jsonPath("$.data.comparison.benefit_changes").isArray())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andDo(print());

            verify(getProductDetailUseCase).execute(1L);
            verify(getProductDetailUseCase).execute(2L);
            verify(planComparator).compare(any(), any(), any(), any());
            verify(planCompareResponseAssembler).assemble(any(ProductDetailResult.class), any(ProductDetailResult.class), any(ComparisonResultDto.class));
        }

        @Test
        @DisplayName("실패: currentPlanId 누락 시 400 Bad Request 반환")
        void whenCurrentPlanIdMissing_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/customer/plans/compare").param("targetPlanId", "2"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: targetPlanId 누락 시 400 Bad Request 반환")
        void whenTargetPlanIdMissing_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/customer/plans/compare").param("currentPlanId", "1"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("공통 – ApiResponse·Content-Type")
    class Common {

        @Test
        @DisplayName("목록 응답에 status·data·timestamp 필드 존재, Content-Type application/json")
        void listResponse_hasStatusDataTimestamp_andJsonContentType() throws Exception {
            ProductListResult result = new ProductListResult(
                    new PageImpl<>(List.of(), PageRequest.of(0, 20), 0),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
            when(getProductListUseCase.execute(eq("mobile"), eq(0), eq(20), eq(0))).thenReturn(result);
            when(productListResponseAssembler.assemble(result))
                    .thenReturn(new ProductListResponse(new PageMeta(0L, 0, 0, 20), List.of()));

            mockMvc.perform(get("/api/v1/customer/plans").param("category", "mobile"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("상세 응답에 status·data·timestamp 필드 존재, Content-Type application/json")
        void detailResponse_hasStatusDataTimestamp_andJsonContentType() throws Exception {
            ProductDetailResult result = new ProductDetailResult(
                    summaryDto(1L, "상품", ProductType.MOBILE_PLAN),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
            when(getProductDetailUseCase.execute(1L)).thenReturn(result);
            when(mapper.toDetailResponse(result)).thenReturn(detailResponse(1L, "상품", "MOBILE_PLAN"));

            mockMvc.perform(get("/api/v1/customer/plans/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }
}
