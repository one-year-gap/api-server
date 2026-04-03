package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import site.holliverse.admin.query.jooq.enums.ProductTypeEnum;

import java.util.List;

import static site.holliverse.admin.query.jooq.Tables.ADDRESS;
import static site.holliverse.admin.query.jooq.Tables.MEMBER;
import static site.holliverse.admin.query.jooq.Tables.PRODUCT;
import static site.holliverse.admin.query.jooq.Tables.SUBSCRIPTION;

/**
 * 지역별 Top 요금제/가입자 수 집계를 담당하는 DAO.
 * - 전지역 단위로 한 번에 집계한다.
 * - 기준 데이터는 활성 구독만 사용한다.
 *
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
     * 정렬 기준(지역 내부):
     * 1) 요금제 가입자 수 내림차순
     * 2) 요금제명 오름차순 (이유 : 요금제명 오름차순을 안하면 같은 구독자일시 매번 요금제명이 달라질 수 있음)
     * 반환:
     * 지역과 top3 요금제
     */
    public List<RegionalTopPlanRawData> findTopPlansByAllProvinces(int limit) {
        Field<String> province = ADDRESS.PROVINCE.as("province");
        Field<Long> memberId = MEMBER.MEMBER_ID.as("memberId");
        Field<String> planName = PRODUCT.NAME.as("planName");

        //1. 먼저 지역 + 회원 + 요금제 기준으로 중복을 제거
        // 원래 countDistinct(member_id)로 하던 일을 앞단의 distinct로 옮긴 단계였음.
        Table<?> base = dsl
                .selectDistinct(province, memberId, planName)
                .from(SUBSCRIPTION)
                .join(MEMBER).on(MEMBER.MEMBER_ID.eq(SUBSCRIPTION.MEMBER_ID))
                .join(ADDRESS).on(ADDRESS.ADDRESS_ID.eq(MEMBER.ADDRESS_ID))
                .join(PRODUCT).on(PRODUCT.PRODUCT_ID.eq(SUBSCRIPTION.PRODUCT_ID))
                .where(SUBSCRIPTION.STATUS.isTrue())
                .and(PRODUCT.PRODUCT_TYPE.eq(ProductTypeEnum.MOBILE_PLAN))
                .asTable("base");
        Field<String> baseProvince = base.field("province", String.class);
        Field<String> basePlanName = base.field("planName", String.class);

        //2. 중복 제거된 결과에서 지역 + 요금제별 가입자 수를 카운트
        // 개선안 : 여기서는 이미 회원 중복이 제거 됐으므로 countDistinct가 아니라 count(*)면 됨
        Field<Integer> subscriberCount = DSL.count().as("subscriberCount");
        Table<?> planCounts = dsl
                .select(
                        baseProvince,
                        basePlanName,
                        subscriberCount
                )
                .from(base)
                .groupBy(baseProvince, basePlanName)
                .asTable("planCounts");

        Field<String> countedProvince = planCounts.field("province", String.class);
        Field<String> countedPlanName = planCounts.field("planName", String.class);
        Field<Integer> countedSubscriberCount = planCounts.field("subscriberCount", Integer.class);

        // 3. 지역별로 가입자 수 내림차순, 요금제명 오름차순 기준 rank를 만듬
        Field<Integer> rankNo = DSL.rowNumber().over(
                DSL.partitionBy(countedProvince)
                        .orderBy(countedSubscriberCount.desc(), countedPlanName.asc())
        ).as("rankNo");

        Table<?> ranked = dsl
                .select(
                        countedProvince,
                        countedPlanName,
                        rankNo
                )
                .from(planCounts)
                .asTable("ranked");

        Field<String> rankedProvince = ranked.field("province", String.class);
        Field<String> rankedPlanName = ranked.field("planName", String.class);
        Field<Integer> rankedRankNo = ranked.field("rankNo", Integer.class);

        // 4. 지역별 상위 N개만 추출하고 기존 DTO 형태로 반환
        return dsl
                .select(rankedProvince, rankedPlanName)
                .from(ranked)
                .where(rankedRankNo.le(limit))
                .orderBy(rankedProvince.asc(), rankedRankNo.asc())
                .fetch(r -> new RegionalTopPlanRawData(
                        r.get(rankedProvince),
                        r.get(rankedPlanName)
                ));

    }

    /**
     * 전지역 총 가입자 수를 조회한다.
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
