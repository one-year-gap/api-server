package site.holliverse.admin.web.dto.analytics;

import java.util.List;

/**==========================
 * 지역 통계 응답 DTO
 *
 * @param regions 지역별 평균 지표 목록
 * @param axisMax 차트 축 최대값 정보
 * @param maxRegion 최대 지표 지역 정보
 * @author nonstop
 * @version 1.0.0
 * @since 2026-02-23
 * ========================== */
public record AdminRegionalMetricResponseDto(
        List<RegionMetricDto> regions,
        AxisMaxDto axisMax,
        MaxRegionDto maxRegion
) {
    /**==========================
     * @param regionCode 지역 코드
     * @param region 지역명
     * @param averageSales 지역 평균 매출
     * @param averageDataUsageGb 지역 평균 데이터 사용량(GB)
     * ========================== */
    public record RegionMetricDto(
            String regionCode,
            String region,
            long averageSales,
            double averageDataUsageGb
    ) {
    }

    /**==========================
     * 차트 축 최대값 DTO
     *
     * @param salesAxisMax 매출 축 최대값
     * @param dataUsageAxisMaxGb 데이터 사용량 축 최대값(GB)
     * ========================== */
    public record AxisMaxDto(
            long salesAxisMax,
            long dataUsageAxisMaxGb
    ) {
    }

    /**==========================
     * 최대 지표 지역 DTO
     *
     * @param salesRegion 평균 매출 최대 지역
     * @param dataUsageRegion 평균 데이터 사용량 최대 지역
     * ========================== */
    public record MaxRegionDto(
            String salesRegion,
            String dataUsageRegion
    ) {
    }
}
