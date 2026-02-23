package site.holliverse.admin.web.dto.analytics;

import java.util.List;

public record AdminRegionalMetricResponseDto(
        // 지역별 평균 지표 목록
        List<RegionMetricDto> regions,
        // 차트 축 최대값(시각화 스케일 기준값)
        AxisMaxDto axisMax,
        // 각 지표에서 최대값을 가진 지역명
        MaxRegionDto maxRegion
) {
    public record RegionMetricDto(
            // 지역명
            String region,
            // 해당 지역의 평균 매출
            long averageSales,
            // 해당 지역의 평균 데이터 사용량(GB)
            long averageDataUsageGb
    ) {
    }

    public record AxisMaxDto(
            // 매출 축 최대값 ex)50000
            long salesAxisMax,
            // 데이터 사용량 축 최대값(GB)
            long dataUsageAxisMaxGb
    ) {
    }

    public record MaxRegionDto(
            // 평균 매출이 가장 높은 지역
            String salesRegion,
            // 평균 데이터 사용량이 가장 높은 지역
            String dataUsageRegion
    ) {
    }
}
