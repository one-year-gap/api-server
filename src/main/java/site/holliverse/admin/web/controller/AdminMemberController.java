package site.holliverse.admin.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.holliverse.admin.application.usecase.*;
import site.holliverse.admin.application.usecase.RetrieveMemberUseCase.RetrieveMemberResult;
import site.holliverse.admin.query.dao.MemberDetailRawData;
import site.holliverse.admin.web.assembler.AdminMemberAssembler;
import site.holliverse.admin.web.dto.member.*;
import site.holliverse.admin.web.mapper.AdminMemberMapper;
import site.holliverse.shared.web.response.ApiResponse;
import jakarta.validation.Valid;

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
    private final GetMemberDetailUseCase getMemberDetailUseCase;
    private final AdminMemberMapper adminMemberMapper;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final BulkUpdateMemberStatusUseCase bulkUpdateMemberStatusUseCase;
    private final GetMembershipCountUseCase getMembershipCountUseCase;

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

    /**
     * 회원 상세 정보 조회 API
     * @param memberId 조회할 회원의 고유 ID
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<AdminMemberDetailResponseDto>> getMemberDetail(@PathVariable("memberId") Long memberId) {

        // 1. UseCase 호출: DB에서 상세 데이터 수집 (없으면 여기서 404 예외 발생)
        MemberDetailRawData rawData = getMemberDetailUseCase.execute(memberId);

        // 2. Mapper 호출: 복호화, 나이/기간 계산 및 DTO 조립
        AdminMemberDetailResponseDto data = adminMemberMapper.toResponse(rawData);

        return ResponseEntity.ok(ApiResponse.success("회원 상세 조회가 완료되었습니다.", data));
    }

    /**
     * 회원 정보 수정 API
     * @param memberId 수정할 회원의 고유 ID
     * @param requestDto 수정할 필드 (값이 null 인 필드는 수정하지 않음)
     */
    @PatchMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> updateMember(
            @PathVariable("memberId") Long memberId,
            @Valid @RequestBody AdminMemberUpdateRequestDto requestDto
    ) {

        // 1. UseCase 호출: 회원 검증, 필드 암호화 및 DB 동적 업데이트 실행
        updateMemberUseCase.execute(memberId, requestDto);

        // 2. 응답 반환: 데이터(Body) 없이 성공 메시지만 깔끔하게 전달
        return ResponseEntity.ok(ApiResponse.success("회원 정보 수정이 완료되었습니다.", null));
    }

    /**
     * 회원 상태 일괄 변경 API (Bulk Update)
     * @param requestDto 변경할 대상 회원 ID 배열과 상태값 (최대 100명)
     */
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<Integer>> updateMembersStatus(
            @Valid @RequestBody AdminMemberBulkStatusUpdateRequestDto requestDto
    ) {

        // 1. UseCase 호출: 검증 및 DB 일괄 업데이트 실행 후, 처리된 건수 반환
        int updatedCount = bulkUpdateMemberStatusUseCase.execute(requestDto);

        // 2. 응답 반환: 성공 메시지와 함께 업데이트된 회원 수를 data 영역에 담아 전달
        return ResponseEntity.ok(ApiResponse.success(
                updatedCount + "명의 회원 상태가 변경되었습니다.",
                updatedCount
        ));
    }

    /**
     * 회원 총 수, 등급별 인원수 반환
     */
    @GetMapping("/membership")
    public ResponseEntity<ApiResponse<TotalMembershipResponseDto>> totalMemberships() {

        TotalMembershipResponseDto data = getMembershipCountUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success("멤버십 통계 조회가 완료되었습니다.", data));
    }
}
