package site.holliverse.admin.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.admin.application.usecase.RetrieveMemberUseCase;
import site.holliverse.admin.application.usecase.RetrieveMemberUseCase.RetrieveMemberResult;
import site.holliverse.admin.web.assembler.AdminMemberAssembler;
import site.holliverse.admin.web.dto.member.AdminMemberListRequestDto;
import site.holliverse.admin.web.dto.member.AdminMemberListResponseDto;
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

import site.holliverse.admin.application.usecase.GetMemberDetailUseCase;
import site.holliverse.admin.web.dto.member.AdminMemberDetailResponseDto;
import site.holliverse.admin.web.mapper.AdminMemberMapper;
import site.holliverse.admin.query.dao.MemberDetailRawData;
import java.time.LocalDate;
import static org.mockito.BDDMockito.mock;

@ActiveProfiles("admin")
@WebMvcTest(AdminMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RetrieveMemberUseCase retrieveMemberUseCase;

    @MockitoBean
    private AdminMemberAssembler adminMemberAssembler;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean private GetMemberDetailUseCase getMemberDetailUseCase;
    @MockitoBean private AdminMemberMapper adminMemberMapper;

    @Test
    @DisplayName("회원 목록 조회 성공 시 ApiResponse 규격에 맞춰 데이터를 반환한다.")
    void getMemberList_success() throws Exception {
        // given
        // 1. UseCase가 반환할 가짜 데이터 (Raw Data)
        RetrieveMemberResult mockResult = new RetrieveMemberResult(Collections.emptyList(), 0);
        given(retrieveMemberUseCase.execute(any(AdminMemberListRequestDto.class)))
                .willReturn(mockResult);

        // 2. Assembler가 반환할 가짜 데이터 (가공된 DTO)
        AdminMemberListResponseDto mockResponse = AdminMemberListResponseDto.builder()
                .members(List.of()) // 빈 리스트
                .pagination(AdminMemberListResponseDto.Pagination.builder()
                        .totalCount(0)
                        .currentPage(1)
                        .size(20)
                        .totalPage(0)
                        .build())
                .build();

        given(adminMemberAssembler.toListResponse(any(), anyInt(), anyInt(), anyInt()))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/admin/members")
                        .param("page", "1")
                        .param("size", "20")
                        .param("keyword", "김영현")) // 검색어 파라미터 예시
                .andDo(print()) // 테스트 실행 로그 출력
                .andExpect(status().isOk()) // HTTP 200 확인

                // ApiResponse 공통 규격 검증
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("회원 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists())

                // 내부 데이터 검증 (Pagination 등)
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.size").value(20));
    }

    @Test
    @DisplayName("회원 상세 조회 성공 시 200 OK와 회원 상세 데이터를 반환한다.")
    void getMemberDetail_success() throws Exception {
        // given
        Long memberId = 2L;

        // UseCase와 Mapper가 주고받을 가짜 객체들
        MemberDetailRawData mockRaw = mock(MemberDetailRawData.class);
        AdminMemberDetailResponseDto mockResponse = new AdminMemberDetailResponseDto(
                "김영현", 31, "VIP", "M", "경기도 구리시", "test@test.com",
                LocalDate.of(1995, 1, 1), "5G 요금제", "010-1234-5678",
                LocalDate.of(2024, 1, 1), 416L, "ACTIVE"
        );

        given(getMemberDetailUseCase.execute(memberId)).willReturn(mockRaw);
        given(adminMemberMapper.toResponse(mockRaw)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/admin/members/{memberId}", memberId))
                .andDo(print())
                .andExpect(status().isOk())

                // ApiResponse 규격 검증
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("회원 상세 조회가 완료되었습니다."))

                // 데이터 검증
                .andExpect(jsonPath("$.data.name").value("김영현"))
                .andExpect(jsonPath("$.data.currentMobilePlan").value("5G 요금제"))
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"));
    }
}