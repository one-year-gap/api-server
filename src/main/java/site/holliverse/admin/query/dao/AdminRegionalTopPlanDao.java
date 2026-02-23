package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

import static site.holliverse.admin.query.jooq.Tables.ADDRESS;
import static site.holliverse.admin.query.jooq.Tables.MEMBER;
import static site.holliverse.admin.query.jooq.Tables.PRODUCT;
import static site.holliverse.admin.query.jooq.Tables.SUBSCRIPTION;

/**
 * 지역별 Top 요금제/가입자 수 집계를 담당하는 DAO.
 * - 전지역 단위로 한 번에 집계한다.
 * - 기준 데이터는 활성 구독만 사용한다.
 * @author nonstop
 * @version 1.0.0
 * @since 2026-02-23
 */
@Profile("admin")
@Repository
@RequiredArgsConstructor
public class AdminRegionalTopPlanDao {

    // jOOQ 쿼리 실행 진입점
    private final DSLContext dsl;

    /**
     * 전지역의 Top N 요금제명을 조회한다.
     *
     * 정렬 기준(지역 내부):
     * 1) 요금제 가입자 수 내림차순
     * 2) 요금제명 오름차순 (이유 : 요금제명 오름차순을 안하면 같은 구독자일시 매번 요금제명이 달라질 수 있음)
     *
     * 반환:
     * 지역과 top3 요금제
     */
    public List<RegionalTopPlanRawData> findTopPlansByAllProvinces(int limit) {
        Field<String> province = ADDRESS.PROVINCE.as("province");
        Field<String> planName = PRODUCT.NAME.as("planName");
        // 지역별 순위를 만들기 위한 윈도우 함수(row_number)
        Field<Integer> rankNo = DSL.rowNumber().over(
                DSL.partitionBy(ADDRESS.PROVINCE)
                        .orderBy(DSL.countDistinct(MEMBER.MEMBER_ID).desc(), PRODUCT.NAME.asc())
        ).as("rankNo");

        // 1단계: (지역, 요금제)별 가입자 수를 집계하고 지역 내부 순위를 계산
        Table<?> ranked = dsl
                .select(province, planName, rankNo)
                .from(SUBSCRIPTION)
                .join(MEMBER).on(MEMBER.MEMBER_ID.eq(SUBSCRIPTION.MEMBER_ID))
                .join(ADDRESS).on(ADDRESS.ADDRESS_ID.eq(MEMBER.ADDRESS_ID))
                .join(PRODUCT).on(PRODUCT.PRODUCT_ID.eq(SUBSCRIPTION.PRODUCT_ID))
                .where(SUBSCRIPTION.STATUS.isTrue())
                .groupBy(ADDRESS.PROVINCE, PRODUCT.NAME)
                .asTable("ranked");

        Field<String> rankedProvince = ranked.field(province);
        Field<String> rankedPlanName = ranked.field(planName);
        Field<Integer> rankedRankNo = ranked.field(rankNo);

        // 2단계: 지역별 순위가 limit 이하인 데이터만 추출
        return dsl
                .select(
                        rankedProvince,
                        rankedPlanName
                )
                .from(ranked)
                .where(rankedRankNo.le(limit))
                .orderBy(
                        rankedProvince.asc(),
                        rankedRankNo.asc()
                )
                .fetch(r -> new RegionalTopPlanRawData(
                        r.get(rankedProvince),
                        r.get(rankedPlanName)
                ));
    }

    /**
     * 전지역 총 가입자 수를 조회한다.
     *
     * 규칙:
     * - distinct(member_id) 기준
     * - 활성 구독(status=true)만 포함
     */
    public List<RegionalSubscriberCountRawData> findSubscriberCountsByAllProvinces() {
        Field<Integer> subscriberCount = DSL.countDistinct(MEMBER.MEMBER_ID).as("subscriberCount");

        return dsl
                .select(ADDRESS.PROVINCE.as("province"), subscriberCount)
                .from(SUBSCRIPTION)
                .join(MEMBER).on(MEMBER.MEMBER_ID.eq(SUBSCRIPTION.MEMBER_ID))
                .join(ADDRESS).on(ADDRESS.ADDRESS_ID.eq(MEMBER.ADDRESS_ID))
                .where(SUBSCRIPTION.STATUS.isTrue())
                .groupBy(ADDRESS.PROVINCE)
                .orderBy(ADDRESS.PROVINCE.asc())
                .fetch(r -> new RegionalSubscriberCountRawData(
                        r.get("province", String.class),
                        toLong(r.get(subscriberCount))
                ));
    }

    private long toLong(Integer value) {
        return value == null ? 0L : value.longValue();
    }
}
