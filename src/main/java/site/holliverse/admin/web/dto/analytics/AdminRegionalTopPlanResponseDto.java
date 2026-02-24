package site.holliverse.admin.web.dto.analytics;

import java.util.List;

/**==========================
 * @param regions 지역 리스트
 * @author nonstop
 * @version 1.0.0
 * @since 2026-02-23
 * ========================== */

public record AdminRegionalTopPlanResponseDto(
        List<RegionTopPlanDto> regions
) {
    /**==========================
     * @param region 지역
     * @param regionCode 지역 코드
     * @param topPlans 3가지 지역코드
     * @param regionalSubscriberCount 지역 구독자수
     * @author nonstop
     * @version 1.0.0
     * @since 2026-02-23
     * ==========================     */
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
