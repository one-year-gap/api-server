package site.holliverse.admin.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.admin.application.usecase.CounselTrafficUseCase;
import site.holliverse.admin.query.dao.CounselTrafficDailyRawData;
import site.holliverse.admin.query.dao.CounselTrafficMonthlyRawData;
import site.holliverse.admin.web.dto.counsel.CounselTrafficMonthResponseDto;
import site.holliverse.admin.web.dto.counsel.CounselTrafficTDailyResponseDto;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.web.response.ApiResponse;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@Profile("admin")
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/counsel-traffic")
public class CounselController {
    private final CounselTrafficUseCase useCase;

    /**
     * 일 상담 트래픽 조회
     * @param date 'yyyy-MM-dd'
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<CounselTrafficTDailyResponseDto>> getDailyTraffic(
             @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<CounselTrafficDailyRawData> data = useCase.getHourlyTraffic(date);
        return ResponseEntity.ok(ApiResponse.success(
                "상담 트래픽 조회가 완료되었습니다. (일/시간 기준)",
                CounselTrafficTDailyResponseDto.of(data)
        ));
    }

    /**
     * 월 상담 트래픽 조회
     * @param month 월
     */
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<CounselTrafficMonthResponseDto>> getMonthlyTraffic(
            @RequestParam("month") YearMonth month
    ) {
        List<CounselTrafficMonthlyRawData> data = useCase.getDailyTraffic(month);
        return ResponseEntity.ok(ApiResponse.success(
                "상담 트래픽 조회가 완료되었습니다. (월/일 기준)",
                CounselTrafficMonthResponseDto.of(data)
        ));
    }
}
