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
import site.holliverse.admin.application.usecase.GetSupportStatUseCase;
import site.holliverse.admin.query.dao.AdminSupportStatRawData;
import site.holliverse.admin.web.dto.support.AdminSupportStatResponseDto;
import site.holliverse.admin.web.mapper.AdminSupportStatMapper;
import site.holliverse.auth.jwt.JwtTokenProvider;

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
}