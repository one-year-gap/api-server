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
import site.holliverse.admin.web.dto.analytics.AdminRegionalTopPlanResponseDto;
import site.holliverse.auth.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @DisplayName("Regional ARPU API returns expected schema.")
    void getRegionalMetrics_success() throws Exception {
        List<RegionalMetricRawData> raw = List.of(
                new RegionalMetricRawData("SEOUL", BigDecimal.valueOf(45454), BigDecimal.valueOf(9876))
        );
        AdminRegionalMetricResponseDto response = new AdminRegionalMetricResponseDto(
                List.of(new AdminRegionalMetricResponseDto.RegionMetricDto("R001", "SEOUL", 45454, 9876)),
                new AdminRegionalMetricResponseDto.AxisMaxDto(50000, 10000),
                new AdminRegionalMetricResponseDto.MaxRegionDto("SEOUL", "SEOUL")
        );

        given(retrieveRegionalMetricUseCase.execute(any(AdminRegionalMetricRequestDto.class))).willReturn(raw);
        given(adminRegionalMetricAssembler.toResponse(raw)).willReturn(response);

        mockMvc.perform(get("/api/v1/admin/analytics/regions/arpu").param("yyyymm", "202601"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.axisMax.salesAxisMax").value(50000))
                .andExpect(jsonPath("$.data.maxRegion.salesRegion").value("SEOUL"));
    }

    @Test
    @DisplayName("전지역 Top3 요금제 API가 응답 규격에 맞게 반환된다.")
    void getTopPlansByAllRegions_success() throws Exception {
        List<RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary> summaries = List.of(
                new RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary(
                        "SEOUL",
                        500L,
                        List.of("PLAN_A", "PLAN_B", "PLAN_C")
                )
        );
        AdminRegionalTopPlanResponseDto response = new AdminRegionalTopPlanResponseDto(
                List.of(
                        new AdminRegionalTopPlanResponseDto.RegionTopPlanDto(
                                "R001",
                                "SEOUL",
                                500L,
                                List.of(
                                        new AdminRegionalTopPlanResponseDto.TopPlanDto("PLAN_A"),
                                        new AdminRegionalTopPlanResponseDto.TopPlanDto("PLAN_B"),
                                        new AdminRegionalTopPlanResponseDto.TopPlanDto("PLAN_C")
                                )
                        )
                )
        );

        given(retrieveRegionalTopPlanUseCase.execute()).willReturn(summaries);
        given(adminRegionalTopPlanAssembler.toResponse(summaries)).willReturn(response);

        mockMvc.perform(get("/api/v1/admin/analytics/regions/plans/top3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.regions[0].region").value("SEOUL"))
                .andExpect(jsonPath("$.data.regions[0].regionalSubscriberCount").value(500))
                .andExpect(jsonPath("$.data.regions[0].topPlans[0].planName").value("PLAN_A"));
    }
}
