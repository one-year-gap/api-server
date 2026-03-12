package site.holliverse.admin.web.dto.analytics;

import java.util.List;

/**
 * 페르소나 분포도 및 Top3 요금제 응답
 */
public record PersonaDistributionResponseDto(
        String personaName,
        long userCount,
        double percentage,            // 프론트엔드 화면 표시용 비율 (예: 33.3)
        List<String> top3PlanNames    // 해당 페르소나가 많이 쓰는 요금제 Top 3
) {
}