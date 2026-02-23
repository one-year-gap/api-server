package site.holliverse.admin.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.admin.application.usecase.RetrieveRegionalMetricUseCase;
import site.holliverse.admin.application.usecase.RetrieveRegionalTopPlanUseCase;
import site.holliverse.admin.web.assembler.AdminRegionalMetricAssembler;
import site.holliverse.admin.web.assembler.AdminRegionalTopPlanAssembler;
import site.holliverse.admin.web.dto.analytics.AdminRegionalMetricRequestDto;
import site.holliverse.admin.web.dto.analytics.AdminRegionalMetricResponseDto;
import site.holliverse.admin.web.dto.analytics.AdminRegionalTopPlanResponseDto;
import site.holliverse.shared.web.response.ApiResponse;

/**
 * 관리자 분석(지역 단위) API 컨트롤러.
 * - ARPU/평균 사용량
 * - 지도 hover용 전지역 Top3 요약
 */
@Profile("admin")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/analytics/regions")
public class AdminRegionalMetricController {

    private final RetrieveRegionalMetricUseCase retrieveRegionalMetricUseCase;
    private final AdminRegionalMetricAssembler adminRegionalMetricAssembler;
    private final RetrieveRegionalTopPlanUseCase retrieveRegionalTopPlanUseCase;
    private final AdminRegionalTopPlanAssembler adminRegionalTopPlanAssembler;

    /**
     * 지역별 평균 매출(ARPU) / 평균 데이터 사용량 조회.
     * yyyymm 파라미터 기준으로 집계한다.
     */
    @GetMapping("/arpu")
    public ResponseEntity<ApiResponse<AdminRegionalMetricResponseDto>> getRegionalMetrics(
            AdminRegionalMetricRequestDto requestDto
    ) {
        AdminRegionalMetricResponseDto data = adminRegionalMetricAssembler.toResponse(
                retrieveRegionalMetricUseCase.execute(requestDto)
        );
        return ResponseEntity.ok(ApiResponse.success("지역 ARPU 조회에 성공했습니다.", data));
    }

    /**
     * 지도 hover용 전지역 요약 조회.
     *
     * 응답:
     * - 지역명
     * - 지역 총 가입자 수
     * - 해당 지역 Top3 요금제
     */
    @GetMapping("/plans/top3")
    public ResponseEntity<ApiResponse<AdminRegionalTopPlanResponseDto>> getTopPlansByAllRegions() {
        AdminRegionalTopPlanResponseDto data = adminRegionalTopPlanAssembler.toResponse(
                retrieveRegionalTopPlanUseCase.execute()
        );
        return ResponseEntity.ok(ApiResponse.success("전지역 Top3 요금제 조회에 성공했습니다.", data));
    }
}
