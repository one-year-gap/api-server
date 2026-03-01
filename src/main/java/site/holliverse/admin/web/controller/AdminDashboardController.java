package site.holliverse.admin.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import site.holliverse.admin.application.usecase.GetKeywordBubbleChartUseCase;
import site.holliverse.admin.application.usecase.GetSupportStatUseCase;
import site.holliverse.admin.query.dao.AdminSupportStatRawData;
import site.holliverse.admin.web.dto.support.AdminSupportStatResponseDto;
import site.holliverse.admin.web.dto.support.KeywordBubbleChartResponseDto;
import site.holliverse.admin.web.mapper.AdminSupportStatMapper;
import site.holliverse.shared.web.response.ApiResponse;

import java.util.List;

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
    private final GetKeywordBubbleChartUseCase getKeywordBubbleChartUseCase;

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

    /**
     * 비즈니스 키워드 버블 차트 통계 조회 API
     * 파라미터가 없으면 '전체 기간', 있으면 '해당 년/월' 기준 TOP 10 통계(증감율 포함)를 반환
     */
    @GetMapping("/supports/keywords")
    public ResponseEntity<ApiResponse<List<KeywordBubbleChartResponseDto>>> getKeywordBubbleChartStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {

        // 1. UseCase 호출: DB 조회 및 전월 대비 증감율(%) 계산 로직 실행
        List<KeywordBubbleChartResponseDto> data = getKeywordBubbleChartUseCase.execute(year, month);

        return ResponseEntity.ok(
                ApiResponse.success("상담 키워드 통계 조회가 완료되었습니다.", data)
        );
    }
}