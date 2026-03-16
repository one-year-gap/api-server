package site.holliverse.admin.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.holliverse.admin.application.usecase.IssueChurnCouponUseCase;
import site.holliverse.admin.application.usecase.RetrieveChurnRiskMemberUseCase;
import site.holliverse.admin.application.usecase.RetrieveChurnRiskMemberUseCase.RetrieveChurnRiskMemberResult;
import site.holliverse.admin.web.assembler.ChurnRiskMemberAssembler;
import site.holliverse.admin.web.dto.churn.ChurnRiskMemberListRequestDto;
import site.holliverse.admin.web.dto.churn.ChurnRiskMemberListResponseDto;
import site.holliverse.admin.web.dto.churn.IssueChurnCouponRequestDto;
import site.holliverse.admin.web.dto.churn.IssueChurnCouponResponseDto;
import site.holliverse.shared.web.response.ApiResponse;

/**
 * 관리자 이탈 위험군 목록 조회 API 컨트롤러.
 * 
 * 역할:
 * - HTTP 요청 파라미터를 DTO로 바인딩
 * - UseCase 실행
 * - Assembler를 통해 최종 응답 DTO 조립
 * - 공통 ApiResponse 형식으로 반환
 *  대시보드 조회 , 쿠폰 발급
 */
@Profile("admin")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/churn-risk")
public class AdminChurnRiskController {

    private final RetrieveChurnRiskMemberUseCase retrieveChurnRiskMemberUseCase;
    private final ChurnRiskMemberAssembler churnRiskMemberAssembler;
    private final IssueChurnCouponUseCase issueChurnCouponUseCase;

    /**
     * 이탈 위험군 목록 조회 API.
     * <p>
     * 지원하는 query parameter 예시:
     * - page=1
     * - size=10
     * - keyword=홍길동
     * - memberships=VIP&memberships=GOLD
     * - riskLevels=HIGH&riskLevels=MEDIUM
     */
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<ChurnRiskMemberListResponseDto>> getChurnRiskMembers(
            @Valid ChurnRiskMemberListRequestDto requestDto
    ) {
        RetrieveChurnRiskMemberResult result = retrieveChurnRiskMemberUseCase.execute(requestDto);

        ChurnRiskMemberListResponseDto data = churnRiskMemberAssembler.toListResponse(
                result.members(),
                result.totalCount(),
                requestDto.page(),
                requestDto.size()
        );

        return ResponseEntity.ok(ApiResponse.success("이탈 위험군 목록 조회가 완료되었습니다.", data));
    }

    @PostMapping("/coupons/issue")
    public ResponseEntity<ApiResponse<IssueChurnCouponResponseDto>> issueChurnCoupon(
            @Valid @RequestBody IssueChurnCouponRequestDto requestDto
    ) {
        IssueChurnCouponResponseDto data = issueChurnCouponUseCase.execute(requestDto);

        return ResponseEntity.ok(
                ApiResponse.success("이탈 위험군 쿠폰 발송이 완료되었습니다.",data)
        );
    }
}
