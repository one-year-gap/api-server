package site.holliverse.admin.query.dao;

/**
 * 지역별 총 가입자 수 쿼리의 단일 행(raw) 결과.
 */
public record RegionalSubscriberCountRawData(
        String province,
        long regionalSubscriberCount
) {
}
