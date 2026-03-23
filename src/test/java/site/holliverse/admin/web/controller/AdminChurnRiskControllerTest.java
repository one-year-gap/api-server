package site.holliverse.admin.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.admin.application.usecase.IssueChurnCouponUseCase;
import site.holliverse.admin.application.usecase.RetrieveChurnRiskMemberUseCase;
import site.holliverse.admin.application.usecase.RetrieveChurnRiskMemberUseCase.RetrieveChurnRiskMemberResult;
import site.holliverse.admin.web.assembler.ChurnRiskMemberAssembler;
import site.holliverse.admin.web.dto.churn.ChurnRiskMemberDto;
import site.holliverse.admin.web.dto.churn.ChurnRiskMemberListResponseDto;
import site.holliverse.admin.web.dto.churn.IssueChurnCouponRequestDto;
import site.holliverse.admin.web.dto.churn.IssueChurnCouponResponseDto;
import site.holliverse.admin.web.dto.churn.SkippedCouponIssueMemberDto;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.shared.error.ApiErrorResponseFactory;
import site.holliverse.shared.error.ConstraintExceptionMapper;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("admin")
@WebMvcTest(AdminChurnRiskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiErrorResponseFactory.class, ConstraintExceptionMapper.class})
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
    private IssueChurnCouponUseCase issueChurnCouponUseCase;

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

    @Test
    @DisplayName("이탈 위험군 쿠폰 발송 성공 시 발송 결과를 반환한다.")
    void issueChurnCoupon_success() throws Exception {
        IssueChurnCouponRequestDto requestDto = new IssueChurnCouponRequestDto(
                List.of(1L, 3L),
                1L
        );

        IssueChurnCouponResponseDto responseDto = new IssueChurnCouponResponseDto(
                2,
                1,
                1,
                List.of(1L),
                List.of(new SkippedCouponIssueMemberDto(3L, "ALREADY_ISSUED_WITHIN_90_DAYS"))
        );

        given(issueChurnCouponUseCase.execute(any(IssueChurnCouponRequestDto.class)))
                .willReturn(responseDto);

        mockMvc.perform(post("/api/v1/admin/churn-risk/coupons/issue")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("이탈 위험군 쿠폰 발송이 완료되었습니다."))
                .andExpect(jsonPath("$.data.requestedCount").value(2))
                .andExpect(jsonPath("$.data.issuedCount").value(1))
                .andExpect(jsonPath("$.data.skippedCount").value(1))
                .andExpect(jsonPath("$.data.issuedMemberIds[0]").value(1))
                .andExpect(jsonPath("$.data.skippedMembers[0].memberId").value(3))
                .andExpect(jsonPath("$.data.skippedMembers[0].reason").value("ALREADY_ISSUED_WITHIN_90_DAYS"));
    }

    @Test
    @DisplayName("이탈 위험군 쿠폰 발송 실패: memberIds가 비어 있으면 400을 반환한다.")
    void issueChurnCoupon_fail_emptyMemberIds() throws Exception {
        String invalidBody = """
                {
                  "memberIds": [],
                  "couponId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/admin/churn-risk/coupons/issue")
                        .contentType("application/json")
                        .content(invalidBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorDetail.field").value("memberIds"));
    }
}
