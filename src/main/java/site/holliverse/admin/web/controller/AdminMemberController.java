package site.holliverse.admin.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.admin.application.usecase.RetrieveMemberUseCase;
import site.holliverse.admin.application.usecase.RetrieveMemberUseCase.RetrieveMemberResult;
import site.holliverse.admin.web.assembler.AdminMemberAssembler;
import site.holliverse.admin.web.dto.member.AdminMemberListRequestDto;
import site.holliverse.admin.web.dto.member.AdminMemberListResponseDto;
import site.holliverse.shared.web.response.ApiResponse;

import java.time.LocalDateTime;

/**
 * 관리자 회원 관리 API 컨트롤러
 * - Web 계층에서 DTO 조립, 복호화, 마스킹 로직을 어셈블러를 통해 실행.
 */
@Profile("admin")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/members")
public class AdminMemberController {

    private final RetrieveMemberUseCase retrieveMemberUseCase;
    private final AdminMemberAssembler adminMemberAssembler;

    /**
     * 회원 목록 조회 API
     * @param requestDto 페이징 및 검색 조건
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AdminMemberListResponseDto>> getMemberList(AdminMemberListRequestDto requestDto) {

        // 1. UseCase 호출: DB에서 Raw 데이터(암호문 상태) 수집
        RetrieveMemberResult result = retrieveMemberUseCase.execute(requestDto);

        // 2. Assembler 호출: 복호화, 마스킹, 페이징 계산 및 DTO 조립
        AdminMemberListResponseDto data = adminMemberAssembler.toListResponse(
                result.members(),
                result.totalCount(),
                requestDto.page(),
                requestDto.size()
        );

        return ResponseEntity.ok(ApiResponse.success("회원 목록 조회가 완료되었습니다.", data));
    }
}