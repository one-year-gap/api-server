package site.holliverse.admin.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.admin.application.usecase.RetrieveChurnRealtimeUseCase;
import site.holliverse.admin.application.usecase.RetrieveChurnRealtimeUseCase.RetrieveChurnRealtimeResult;
import site.holliverse.admin.web.assembler.ChurnRealtimeAssembler;
import site.holliverse.admin.web.dto.churn.ChurnRealTimeRequestDto;
import site.holliverse.admin.web.dto.churn.ChurnRealTimeResponseDto;
import site.holliverse.shared.web.response.ApiResponse;

@Profile("admin")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/churn")
public class ChurnRealTimeController {
    private final RetrieveChurnRealtimeUseCase retrieveChurnRealtimeUseCase;
    private final ChurnRealtimeAssembler churnRealtimeAssembler;

    /**
     * 대시보드 최초 진입 시 최신 목록 조회
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<ChurnRealTimeResponseDto>> latest(
            @Valid @ModelAttribute ChurnRealTimeRequestDto requestDto
    ) {
        RetrieveChurnRealtimeResult result = retrieveChurnRealtimeUseCase.latest(requestDto);
        ChurnRealTimeResponseDto response = churnRealtimeAssembler.toResponse(result, 0L);

        return ResponseEntity.ok(ApiResponse.success("이탈 위험도 조회가 완료되었습니다.", response));
    }

    /**
     * 폴링 시 마지막 커서 이후 변경분만 조회.
     */
    @GetMapping("/changes")
    public ResponseEntity<ApiResponse<ChurnRealTimeResponseDto>> changes(
           @Valid @ModelAttribute ChurnRealTimeRequestDto requestDto
    ) {
        RetrieveChurnRealtimeResult result = retrieveChurnRealtimeUseCase.changes(requestDto);
        ChurnRealTimeResponseDto response = churnRealtimeAssembler.toResponse(result, requestDto.normalizedAfterId());

        return ResponseEntity.ok(ApiResponse.success("이탈 위험도 조회가 완료되었습니다.", response));
    }
}
