package site.holliverse.admin.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import site.holliverse.admin.application.usecase.GetSupportStatUseCase;
import site.holliverse.admin.query.dao.AdminSupportStatRawData;
import site.holliverse.admin.web.dto.support.AdminSupportStatResponseDto;
import site.holliverse.admin.web.mapper.AdminSupportStatMapper;
import site.holliverse.shared.web.response.ApiResponse;

/**
 * 관리자 대시보드 관련 API를 제공하는 컨트롤러
 */
@Profile("admin")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final GetSupportStatUseCase getSupportStatUseCase;
    private final AdminSupportStatMapper adminSupportStatMapper;

    /**
     * 전체 상담 처리 현황 통계 조회 API
     */
    @GetMapping("/supports/stats")
    public ResponseEntity<ApiResponse<AdminSupportStatResponseDto>> getSupportStatusStats() {

        // 1. UseCase 호출: DB에서 통계 RawData 획득
        AdminSupportStatRawData rawData = getSupportStatUseCase.execute();

        // 2. Mapper 호출: 응답용 DTO로 변환
        AdminSupportStatResponseDto data = adminSupportStatMapper.toResponseDto(rawData);

        return ResponseEntity.ok(
                ApiResponse.success("전체 상담 처리 현황 통계 조회가 완료되었습니다.", data)
        );
    }
}