package site.holliverse.admin.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.admin.application.usecase.RetrieveChurnRiskMemberUseCase;
import site.holliverse.admin.application.usecase.RetrieveChurnRiskMemberUseCase.RetrieveChurnRiskMemberResult;
import site.holliverse.admin.web.assembler.ChurnRiskMemberAssembler;
import site.holliverse.admin.web.dto.churn.ChurnRiskMemberDto;
import site.holliverse.admin.web.dto.churn.ChurnRiskMemberListResponseDto;
import site.holliverse.auth.jwt.JwtTokenProvider;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("admin")
@WebMvcTest(AdminChurnRiskController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminChurnRiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RetrieveChurnRiskMemberUseCase retrieveChurnRiskMemberUseCase;

    @MockitoBean
    private ChurnRiskMemberAssembler churnRiskMemberAssembler;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("이탈 위험군 목록 조회 성공 시 ApiResponse 규격에 맞춰 데이터를 반환한다.")
    void getChurnRiskMembers_success() throws Exception {
        RetrieveChurnRiskMemberResult mockResult = new RetrieveChurnRiskMemberResult(Collections.emptyList(), 0);
        given(retrieveChurnRiskMemberUseCase.execute(any())).willReturn(mockResult);

        ChurnRiskMemberListResponseDto mockResponse = ChurnRiskMemberListResponseDto.builder()
                .members(List.of(
                        ChurnRiskMemberDto.builder()
                                .no(1)
                                .memberId(123L)
                                .membership("VIP")
                                .name("홍*동")
                                .riskLevel("HIGH")
                                .riskReason("요금제 변경 검토")
                                .churnScore(85)
                                .phone("010-****-5678")
                                .email("test@test.com")
                                .build()
                ))
                .pagination(ChurnRiskMemberListResponseDto.Pagination.builder()
                        .totalCount(1)
                        .currentPage(1)
                        .size(10)
                        .totalPage(1)
                        .build())
                .build();

        given(churnRiskMemberAssembler.toListResponse(any(), anyInt(), anyInt(), anyInt()))
                .willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/admin/churn-risk/members")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "홍길동")
                        .param("memberships", "VIP")
                        .param("riskLevels", "HIGH"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("이탈 위험군 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.members[0].no").value(1))
                .andExpect(jsonPath("$.data.members[0].memberId").value(123))
                .andExpect(jsonPath("$.data.members[0].riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.data.members[0].churnScore").value(85))
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.size").value(10));
    }

    @Test
    @DisplayName("이탈 위험군 목록 조회 실패: 유효하지 않은 위험도 필터 전달 시 400 에러를 반환한다.")
    void getChurnRiskMembers_fail_invalidRiskLevel() throws Exception {
        mockMvc.perform(get("/api/v1/admin/churn-risk/members")
                        .param("riskLevels", "LOW"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorDetail.field").value("riskLevels[0]"));
    }
}
