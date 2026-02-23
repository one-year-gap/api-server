package site.holliverse.admin.web.dto.analytics;

import java.util.List;

public record AdminRegionalTopPlanResponseDto(
        List<RegionTopPlanDto> regions
) {
    public record RegionTopPlanDto(
            String regionCode,
            String region,
            long regionalSubscriberCount,
            List<TopPlanDto> topPlans
    ) {
    }

    public record TopPlanDto(
            String planName
    ) {
    }
}
