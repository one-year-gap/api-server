package site.holliverse.admin.web.dto.analytics;

import java.util.List;

/**
 * 전지역 Top3 요금제 요약 API 응답 DTO.
 */
public record AdminRegionalTopPlanResponseDto(
        List<RegionTopPlanDto> regions
) {
    /**
     * 지역별 요약 단위.
     */
    public record RegionTopPlanDto(
            String region,
            long regionalSubscriberCount,
            List<TopPlanDto> topPlans
    ) {
    }

    /**
     * 요금제명 단위.
     */
    public record TopPlanDto(
            String planName
    ) {
    }
}
