package site.holliverse.admin.web.dto.analytics;

import java.util.List;

public record AdminRegionalMetricResponseDto(
        List<RegionMetricDto> regions,
        AxisMaxDto axisMax,
        MaxRegionDto maxRegion
) {
    public record RegionMetricDto(
            String region,
            long averageSales,
            long averageDataUsageGb
    ) {
    }

    public record AxisMaxDto(
            long salesAxisMax,
            long dataUsageAxisMaxGb
    ) {
    }

    public record MaxRegionDto(
            String salesRegion,
            String dataUsageRegion
    ) {
    }
}
