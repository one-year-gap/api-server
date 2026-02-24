package site.holliverse.admin.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.admin.application.usecase.RetrieveRegionalMetricUseCase;
import site.holliverse.admin.application.usecase.RetrieveRegionalTopPlanUseCase;
import site.holliverse.admin.query.dao.RegionalMetricRawData;
import site.holliverse.admin.web.assembler.AdminRegionalMetricAssembler;
import site.holliverse.admin.web.assembler.AdminRegionalTopPlanAssembler;
import site.holliverse.admin.web.dto.analytics.AdminRegionalMetricRequestDto;
import site.holliverse.admin.web.dto.analytics.AdminRegionalMetricResponseDto;
import site.holliverse.auth.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminRegionalMetricController 웹 계층 테스트.
 *
 * 검증 포인트:
 * - /arpu 요청 시 UseCase/Assembler 호출 결과가 응답 스키마에 맞게 반환되는지
 * - regionCode/region/average 값이 JSON 본문으로 정상 직렬화되는지
 */
@ActiveProfiles("admin")
@WebMvcTest(AdminRegionalMetricController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminRegionalMetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RetrieveRegionalMetricUseCase retrieveRegionalMetricUseCase;

    @MockitoBean
    private AdminRegionalMetricAssembler adminRegionalMetricAssembler;

    @MockitoBean
    private RetrieveRegionalTopPlanUseCase retrieveRegionalTopPlanUseCase;

    @MockitoBean
    private AdminRegionalTopPlanAssembler adminRegionalTopPlanAssembler;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("ARPU API는 지역 통계 응답 스키마를 정상 반환한다.")
    void getRegionalMetrics_success() throws Exception {
        List<RegionalMetricRawData> raw = List.of(
                new RegionalMetricRawData("서울특별시", BigDecimal.valueOf(200), BigDecimal.valueOf(15))
        );

        AdminRegionalMetricResponseDto response = new AdminRegionalMetricResponseDto(
                List.of(new AdminRegionalMetricResponseDto.RegionMetricDto("R001", "서울특별시", 200, 15)),
                new AdminRegionalMetricResponseDto.AxisMaxDto(1000, 100),
                new AdminRegionalMetricResponseDto.MaxRegionDto("서울특별시", "서울특별시")
        );

        given(retrieveRegionalMetricUseCase.execute(any(AdminRegionalMetricRequestDto.class))).willReturn(raw);
        given(adminRegionalMetricAssembler.toResponse(raw)).willReturn(response);

        mockMvc.perform(get("/api/v1/admin/analytics/regions/arpu").param("yyyymm", "202602"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.regions[0].regionCode").value("R001"))
                .andExpect(jsonPath("$.data.regions[0].region").value("서울특별시"))
                .andExpect(jsonPath("$.data.regions[0].averageSales").value(200))
                .andExpect(jsonPath("$.data.regions[0].averageDataUsageGb").value(15))
                .andExpect(jsonPath("$.data.axisMax.salesAxisMax").value(1000))
                .andExpect(jsonPath("$.data.maxRegion.salesRegion").value("서울특별시"));
    }
}