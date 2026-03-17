package site.holliverse.admin.web.dto.log;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * POST /api/v1/admin/log-features 요청 DTO.
 * user-logs 배치에서 event_id 기준 중복 제거 후, 비교/위약금 이벤트만 전달한다.
 */
public record LogFeaturesRequestDto(

        @NotNull(message = "memberId는 필수입니다.")
        Long memberId,

        @NotEmpty(message = "events는 최소 1건 이상이어야 합니다.")
        List<LogEventDto> events
) {
    public record LogEventDto(
            @Min(0)
            long eventId,
            @NotBlank(message = "timestamp는 필수입니다.")
            String timestamp,
            @NotBlank(message = "event는 필수입니다.")
            String event,
            @NotBlank(message = "eventName은 필수입니다.")
            String eventName,
            @NotNull(message = "eventProperties는 필수입니다.")
            Map<String, Object> eventProperties
    ) {
    }
}
