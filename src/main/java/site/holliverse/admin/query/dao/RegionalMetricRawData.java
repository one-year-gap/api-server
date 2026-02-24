package site.holliverse.admin.query.dao;

import java.math.BigDecimal;

/**==========================
 * 지역 단위 평균 지표 조회 결과를 담는 DAO Raw DTO.
 *
 * @param province 지역
 * @param avgSales  지역 평균 매출
 * @param avgDataUsageGb 지역 평균 데이터 사용량
 * @author nonstop
 * @version 1.0.0
 * @since 2026-02-23
 * ========================== */
public record RegionalMetricRawData(
        String province,
        BigDecimal avgSales,
        BigDecimal avgDataUsageGb
) {
}
