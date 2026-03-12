package site.holliverse.admin.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import site.holliverse.admin.application.usecase.GetPersonaDashboardUseCase;
import site.holliverse.admin.web.assembler.PersonaDashboardAssembler;
import site.holliverse.admin.web.dto.analytics.PersonaDistributionResponseDto;
import site.holliverse.admin.web.dto.analytics.PersonaMonthlyTrendResponseDto;
import site.holliverse.shared.web.response.ApiResponse;

import java.util.List;

/**
 * 관리자 대시보드 - 페르소나 관련 통계 API를 제공하는 컨트롤러
 */
@Profile("admin")
@RestController
@RequestMapping("/api/v1/admin/dashboard/personas")
@RequiredArgsConstructor
public class PersonaDashboardController {

    private final GetPersonaDashboardUseCase getPersonaDashboardUseCase;
    private final PersonaDashboardAssembler personaDashboardAssembler;

    /**
     * 페르소나 유형별 분포도(%) 및 Top 3 요금제 조회 API
     */
    @GetMapping("/distribution")
    public ResponseEntity<ApiResponse<List<PersonaDistributionResponseDto>>> getDistribution() {

        // 1. UseCase 호출: DB에서 순수 데이터 조회
        var rawDataList = getPersonaDashboardUseCase.getDistribution();

        // 2. Assembler 호출: Web 응답 규격(퍼센트 포함)으로 조립
        var data = personaDashboardAssembler.toDistributionResponses(rawDataList);

        // 3. 공통 ApiResponse 규격으로 감싸서 반환
        return ResponseEntity.ok(
                ApiResponse.success("페르소나 유형별 분포도 조회가 완료되었습니다.", data)
        );
    }

    /**
     * 월별 페르소나 사용자 수 트렌드 (최근 5개월) 조회 API
     */
    @GetMapping("/monthly-trend")
    public ResponseEntity<ApiResponse<List<PersonaMonthlyTrendResponseDto>>> getMonthlyTrend() {

        // 1. UseCase 호출: DB에서 순수 데이터 조회
        var rawDataList = getPersonaDashboardUseCase.getMonthlyTrend();

        // 2. Assembler 호출: Web 응답 규격으로 조립
        var data = personaDashboardAssembler.toMonthlyTrendResponses(rawDataList);

        // 3. 공통 ApiResponse 규격으로 감싸서 반환
        return ResponseEntity.ok(
                ApiResponse.success("월별 페르소나 사용자 수 트렌드 조회가 완료되었습니다.", data)
        );
    }
}