package site.holliverse.admin.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.admin.application.usecase.GetKeywordBubbleChartUseCase;
import site.holliverse.admin.application.usecase.GetSupportStatUseCase;
import site.holliverse.admin.query.dao.AdminSupportStatRawData;
import site.holliverse.admin.web.dto.support.AdminSupportStatResponseDto;
import site.holliverse.admin.web.dto.support.KeywordBubbleChartResponseDto;
import site.holliverse.admin.web.mapper.AdminSupportStatMapper;
import site.holliverse.auth.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("admin")
@WebMvcTest(AdminDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetSupportStatUseCase getSupportStatUseCase;

    @MockitoBean
    private AdminSupportStatMapper adminSupportStatMapper;

    @MockitoBean
    private GetKeywordBubbleChartUseCase getKeywordBubbleChartUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("상담 통계 API 호출 시 200 상태코드와 ApiResponse 규격에 맞게 데이터를 반환한다.")
    void getSupportStatusStats_Success() throws Exception {
        // given: UseCase와 Mapper가 반환할 가짜(Mock) 데이터를 준비
        AdminSupportStatRawData rowData = new AdminSupportStatRawData(10L, 4L, 3L, 3L);
        AdminSupportStatResponseDto responseDto = new AdminSupportStatResponseDto(10L, 4L, 3L, 3L);

        given(getSupportStatUseCase.execute()).willReturn(rowData);
        given(adminSupportStatMapper.toResponseDto(any())).willReturn(responseDto);

        // when & then: GET API를 호출하고 응답(JSON)을 검증
        mockMvc.perform(get("/api/v1/admin/dashboard/supports/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("전체 상담 처리 현황 통계 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.openCount").value(4))
                .andExpect(jsonPath("$.data.supportingCount").value(3))
                .andExpect(jsonPath("$.data.closedCount").value(3));
    }

    @Test
    @DisplayName("키워드 통계 API 호출 시 파라미터가 정상적이면 TOP 10 데이터를 반환한다.")
    void getKeywordBubbleChartStats_Success() throws Exception {
        // given
        List<KeywordBubbleChartResponseDto> mockData = List.of(
                new KeywordBubbleChartResponseDto(1L, "모바일", 100, new BigDecimal("25.00"))
        );
        given(getKeywordBubbleChartUseCase.execute(2026, 3)).willReturn(mockData);

        // when & then
        mockMvc.perform(get("/api/v1/admin/dashboard/supports/keywords")
                        .param("year", "2026")
                        .param("month", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("상담 키워드 통계 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data[0].keywordName").value("모바일"))
                .andExpect(jsonPath("$.data[0].totalCount").value(100))
                .andExpect(jsonPath("$.data[0].changeRate").value(25.00));
    }

    @Test
    @DisplayName("잘못된 월(month=-5)을 입력하면 400 에러와 함께 유효성 검증 실패 응답을 반환한다.")
    void getKeywordBubbleChartStats_InvalidMonth_Fail() throws Exception {
        // given: UseCase까지 가지도 않고 컨트롤러 문지기(@Validated)에서 차단됨

        // when & then
        mockMvc.perform(get("/api/v1/admin/dashboard/supports/keywords")
                        .param("year", "2026")
                        .param("month", "-5") // 잘못된 입력
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorDetail.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errorDetail.reason").exists());
    }

    @Test
    @DisplayName("년도만 입력하고 월을 입력하지 않으면 IllegalArgumentException에 의해 400 에러를 반환한다.")
    void getKeywordBubbleChartStats_IncompleteParams_Fail() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/dashboard/supports/keywords")
                        .param("year", "2026")
                        // month 누락
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorDetail.reason").value("년도와 월은 함께 입력해야 합니다."));
    }
}