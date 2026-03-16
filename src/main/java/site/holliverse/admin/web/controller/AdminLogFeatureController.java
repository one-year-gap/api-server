package site.holliverse.admin.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.admin.application.usecase.LogFeaturesUseCase;
import site.holliverse.admin.web.dto.log.LogFeaturesRequestDto;
import site.holliverse.shared.web.response.ApiResponse;

/**
 * 실시간 로그 기반 feature 카운트 갱신 API.
 * Customer API user-logs 처리 후 event_name dedupe 결과를 전달받아 member_action_feature만 갱신.
 */
@Profile("admin")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminLogFeatureController {

    private final LogFeaturesUseCase logFeaturesUseCase;

    /**
     * 비교/패널티 건수 증분 반영.
     * body: memberId, comparisonIncrement(0|1), penaltyIncrement(0|1).
     * 해당 회원의 MEMBER_ACTION_FEATURE 스냅샷이 있을 때만 갱신, 없으면 no-op.
     */
    @PostMapping("/log-features")
    public ResponseEntity<ApiResponse<Void>> logFeatures(@Valid @RequestBody LogFeaturesRequestDto request) {
        logFeaturesUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success("처리되었습니다.", null));
    }
}
