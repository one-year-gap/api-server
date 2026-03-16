package site.holliverse.admin.web.dto.log;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * POST /api/v1/admin/log-features 요청 DTO.
 * user-logs 배치에서 event_name 기준 중복 제거 후, 비교/패널티 발생 시 각각 0 또는 1로 전달.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogFeaturesRequestDto {

    @NotNull(message = "memberId는 필수입니다.")
    private Long memberId;

    /**
     * 비교하기 클릭 건수 증분 (배치당 0 또는 1)
     */
    @Min(0)
    @Max(1)
    private int comparisonIncrement = 0;

    /**
     * 패널티 확인 건수 증분 (배치당 0 또는 1)
     */
    @Min(0)
    @Max(1)
    private int penaltyIncrement = 0;
}
