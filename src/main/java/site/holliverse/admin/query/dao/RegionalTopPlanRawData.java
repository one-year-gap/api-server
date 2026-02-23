package site.holliverse.admin.query.dao;

/**
 * 지역별 Top 요금제 쿼리의 단일 행(raw) 결과.
 */
public record RegionalTopPlanRawData(
        String province,
        String planName
) {
}
