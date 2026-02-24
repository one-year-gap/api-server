package site.holliverse.admin.query.dao;


/**
 ==========================
 * 각 지역에서 인기있는 top 3 요금제를 반환 받기 위한 RawData
 * @param province 지역명
 * @param planName 요금제 이름
 * @author nonstop
 * @version 1.0.0
 * @since 2026-02-23
 * ========================== */
public record RegionalTopPlanRawData(
        String province,
        String planName
) {
}
