package site.holliverse.admin.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.admin.application.usecase.BulkUpdateMemberStatusUseCase;
import site.holliverse.admin.application.usecase.GetMembershipCountUseCase;
import site.holliverse.admin.application.usecase.RetrieveMemberUseCase;
import site.holliverse.admin.application.usecase.RetrieveMemberUseCase.RetrieveMemberResult;
import site.holliverse.admin.application.usecase.UpdateMemberUseCase;
import site.holliverse.admin.web.assembler.AdminMemberAssembler;
import site.holliverse.admin.web.dto.member.*;
import site.holliverse.auth.jwt.JwtTokenProvider;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import site.holliverse.admin.application.usecase.GetMemberDetailUseCase;
import site.holliverse.admin.web.mapper.AdminMemberMapper;
import site.holliverse.admin.query.dao.MemberDetailRawData;
import java.time.LocalDate;
import java.util.stream.LongStream;

import static org.mockito.BDDMockito.mock;

@ActiveProfiles("admin")
@WebMvcTest(AdminMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RetrieveMemberUseCase retrieveMemberUseCase;

    @MockitoBean
    private AdminMemberAssembler adminMemberAssembler;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean private GetMemberDetailUseCase getMemberDetailUseCase;
    @MockitoBean private AdminMemberMapper adminMemberMapper;
    @MockitoBean private UpdateMemberUseCase updateMemberUseCase;
    @MockitoBean private BulkUpdateMemberStatusUseCase bulkUpdateMemberStatusUseCase;
    @MockitoBean private GetMembershipCountUseCase getMembershipCountUseCase;

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
    @DisplayName("회원 목록 조회 실패: 유효하지 않은 연령대(ages) 필터값 전달 시 400 에러를 반환한다.")
    void getMemberList_fail_invalidAge() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/members")
                        .param("ages", "Tdd"))
                .andDo(print())
                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("유효성 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errorDetail.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errorDetail.field").value("ages[0]"));
    }

    @Test
    @DisplayName("회원 상세 조회 성공 시 200 OK와 회원 상세 데이터를 반환한다.")
    void getMemberDetail_success() throws Exception {
        // given
        Long memberId = 2L;

        // UseCase와 Mapper가 주고받을 가짜 객체들
        MemberDetailRawData mockRaw = mock(MemberDetailRawData.class);
        AdminMemberDetailResponseDto mockResponse = new AdminMemberDetailResponseDto(
                "김영현",
                31,
                "VIP",
                "M",
                "경기도 구리시",
                "test@test.com",
                LocalDate.of(1995, 1, 1),
                "5G 요금제",
                "010-1234-5678",
                LocalDate.of(2024, 1, 1),
                "2년 1개월",
                "ACTIVE",

                // --- 새로 추가된 약정 및 상담 데이터 ---
                true,
                24,
                LocalDate.now().minusMonths(12),
                LocalDate.now().plusMonths(12),
                365,
                false,
                10L,
                LocalDate.now().minusDays(5)
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
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.isContracted").value(true))
                .andExpect(jsonPath("$.data.joinDurationText").value("2년 1개월"));;
    }

    @Test
    @DisplayName("회원 정보 수정 성공: 올바른 데이터가 들어오면 200 OK를 반환한다")
    void updateMember_Success() throws Exception {
        // given
        Long memberId = 1L;
        AdminMemberUpdateRequestDto requestDto = new AdminMemberUpdateRequestDto(
                "김수정", "01012345678", "BANNED", "VIP"
        );

        // UseCase가 아무 일도 하지 않고 정상 종료되도록 설정 (Void 반환이므로 doNothing 사용)
        doNothing().when(updateMemberUseCase).execute(eq(memberId), any(AdminMemberUpdateRequestDto.class));

        // when & then
        mockMvc.perform(patch("/api/v1/admin/members/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("회원 정보 수정이 완료되었습니다."));
    }

    @Test
    @DisplayName("회원 정보 수정 실패: 전화번호에 하이픈(-)이 포함되면 400 Bad Request를 반환한다 (@Valid 검증)")
    void updateMember_Fail_InvalidPhonePattern() throws Exception {
        // given
        Long memberId = 1L;
        // 하이픈이 포함된 잘못된 전화번호 세팅
        AdminMemberUpdateRequestDto requestDto = new AdminMemberUpdateRequestDto(
                "김수정", "010-1234-5678", "BANNED", "VIP"
        );

        // when & then
        mockMvc.perform(patch("/api/v1/admin/members/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원 정보 수정 실패: 이름이 20자를 초과하면 400 에러가 발생한다 (@Size 검증)")
    void updateMember_Fail_NameTooLong() throws Exception {
        // given
        Long memberId = 1L;
        String longName = "이름이매우매우매우매우매우매우매우매우매우매우길어요"; // 20자 초과
        AdminMemberUpdateRequestDto requestDto = new AdminMemberUpdateRequestDto(
                longName, null, null, null
        );

        // when & then
        mockMvc.perform(patch("/api/v1/admin/members/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원 정보 수정 실패: 유효하지 않은 상태값(Enum)이 들어오면 입구에서 막힌다 (@Pattern 검증)")
    void updateMember_Fail_InvalidStatusPattern() throws Exception {
        // given
        Long memberId = 1L;
        AdminMemberUpdateRequestDto requestDto = new AdminMemberUpdateRequestDto(
                "김수정", null, "WEIRD_STATUS", null
        );

        // when & then
        mockMvc.perform(patch("/api/v1/admin/members/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("다중 상태 변경 성공: 올바른 ID 리스트와 상태값이면 200 OK와 처리 건수를 반환한다.")
    void updateMembersStatus_Success() throws Exception {
        // given
        AdminMemberBulkStatusUpdateRequestDto requestDto =
                new AdminMemberBulkStatusUpdateRequestDto(List.of(1L, 2L, 3L), "BANNED");

        // UseCase가 3건을 업데이트했다고 가정
        given(bulkUpdateMemberStatusUseCase.execute(any())).willReturn(3);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/members/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("3명의 회원 상태가 변경되었습니다."))
                .andExpect(jsonPath("$.data").value(3)); // 반환된 데이터가 3인지 확인
    }

    @Test
    @DisplayName("다중 상태 변경 실패: ID 리스트가 비어있으면 400 에러가 발생한다 (@NotEmpty 검증)")
    void updateMembersStatus_Fail_EmptyList() throws Exception {
        // given (빈 배열 전달)
        AdminMemberBulkStatusUpdateRequestDto requestDto =
                new AdminMemberBulkStatusUpdateRequestDto(Collections.emptyList(), "BANNED");

        // when & then
        mockMvc.perform(patch("/api/v1/admin/members/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest()); // 400 에러 확인
    }

    @Test
    @DisplayName("다중 상태 변경 실패: 한 번에 100명을 초과하여 요청하면 400 에러가 발생한다 (@Size 검증)")
    void updateMembersStatus_Fail_ExceedMaxLimit() throws Exception {
        // given (101개의 ID 리스트 생성)
        List<Long> tooManyIds = LongStream.rangeClosed(1, 101).boxed().toList();
        AdminMemberBulkStatusUpdateRequestDto requestDto =
                new AdminMemberBulkStatusUpdateRequestDto(tooManyIds, "ACTIVE");

        // when & then
        mockMvc.perform(patch("/api/v1/admin/members/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("다중 상태 변경 실패: 유효하지 않은 상태값을 보내면 400 에러가 발생한다 (@Pattern 검증)")
    void updateMembersStatus_Fail_InvalidStatus() throws Exception {
        // given (이상한 상태값 전달)
        AdminMemberBulkStatusUpdateRequestDto requestDto =
                new AdminMemberBulkStatusUpdateRequestDto(List.of(1L, 2L), "STRANGE_STATUS");

        // when & then
        mockMvc.perform(patch("/api/v1/admin/members/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("멤버십 통계 조회 성공: totalInK와 등급별 비율을 반환한다")
    void getMembershipStats_success() throws Exception {
        // given
        TotalMembershipResponseDto responseDto = new TotalMembershipResponseDto(
                new java.math.BigDecimal("14.2"),
                new java.math.BigDecimal("33.4"),
                new java.math.BigDecimal("33.3"),
                new java.math.BigDecimal("33.3")
        );
        given(getMembershipCountUseCase.execute()).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/admin/members/membership"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalInK").value(14.2))
                .andExpect(jsonPath("$.data.vvipRate").value(33.4))
                .andExpect(jsonPath("$.data.vipRate").value(33.3))
                .andExpect(jsonPath("$.data.goldRate").value(33.3));
    }
}
