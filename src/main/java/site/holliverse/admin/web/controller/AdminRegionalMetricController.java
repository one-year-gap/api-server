package site.holliverse.admin.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.admin.application.usecase.RetrieveRegionalMetricUseCase;
import site.holliverse.admin.web.assembler.AdminRegionalMetricAssembler;
import site.holliverse.admin.web.dto.analytics.AdminRegionalMetricRequestDto;
import site.holliverse.admin.web.dto.analytics.AdminRegionalMetricResponseDto;
import site.holliverse.shared.web.response.ApiResponse;

@Profile("admin")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/analytics/regions")
public class AdminRegionalMetricController {

    private final RetrieveRegionalMetricUseCase retrieveRegionalMetricUseCase;
    private final AdminRegionalMetricAssembler adminRegionalMetricAssembler;

    @GetMapping("/arpu")
    public ResponseEntity<ApiResponse<AdminRegionalMetricResponseDto>> getRegionalMetrics(
            AdminRegionalMetricRequestDto requestDto
    ) {
        AdminRegionalMetricResponseDto data = adminRegionalMetricAssembler.toResponse(
                retrieveRegionalMetricUseCase.execute(requestDto)
        );
        return ResponseEntity.ok(ApiResponse.success("지역별 평균 매출/데이터 사용량 조회가 완료되었습니다.", data));
    }

}
