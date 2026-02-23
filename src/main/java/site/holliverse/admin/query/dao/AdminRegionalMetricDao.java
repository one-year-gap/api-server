package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static site.holliverse.admin.query.jooq.Tables.ADDRESS;
import static site.holliverse.admin.query.jooq.Tables.MEMBER;
import static site.holliverse.admin.query.jooq.Tables.PRODUCT;
import static site.holliverse.admin.query.jooq.Tables.SUBSCRIPTION;
import static site.holliverse.admin.query.jooq.Tables.USAGE_MONTHLY;

@Profile("admin")
@Repository
@RequiredArgsConstructor
public class AdminRegionalMetricDao {

    // jOOQ 쿼리를 실행하는 핵심 객체
    private final DSLContext dsl;

    /**
     * 지역별 평균 매출/평균 데이터 사용량을 조회한다.
     *
     * 계산 정의:
     * - avgSales: 지역의 총 매출(sale_price 합계) / 지역의 고유 가입자 수
     * - avgDataUsageGb: 지역의 총 데이터 사용량(data_gb 합계) / 지역의 고유 가입자 수
     *
     * 처리 규칙:
     * - 구독은 활성(status = true)만 집계한다.
     * - 데이터 사용량은 요청 월(yyyymm)만 집계한다.
     * - 해당 월 사용량 데이터가 없는 구독자는 데이터 합계에서 0으로 처리한다.
     * - 분모가 0인 경우 nullif/coalesce로 0을 반환해 0 나눗셈을 방지한다.
     */
    public List<RegionalMetricRawData> findRegionalAverages(String yyyymm) {
        // 지역별 매출 합계(분자)
        Field<BigDecimal> totalRevenue = DSL.sum(PRODUCT.SALE_PRICE.cast(BigDecimal.class));

        // usage_details(JSONB)에서 data_gb 값을 numeric으로 안전하게 추출
        // 값이 빈 문자열이면 NULL 처리하여 캐스팅 오류를 방지한다.
        Field<BigDecimal> dataUsageGb = DSL.field(
                "NULLIF({0} ->> 'data_gb', '')::numeric",
                BigDecimal.class,
                USAGE_MONTHLY.USAGE_DETAILS
        );

        // 지역별 데이터 사용량 합계(분자)
        // data_gb가 NULL인 레코드는 0으로 간주한다.
        Field<BigDecimal> totalDataUsage = DSL.sum(DSL.coalesce(dataUsageGb, BigDecimal.ZERO));

        // 지역별 고유 가입자 수(분모)
        // 동일 회원의 중복 구독/조인 중복 영향을 막기 위해 distinct(member_id) 사용
        Field<BigDecimal> subscriberCount = DSL.countDistinct(MEMBER.MEMBER_ID).cast(BigDecimal.class);

        // 평균 매출 = 총매출 / 가입자수
        Field<BigDecimal> avgSales = DSL.coalesce(
                totalRevenue.div(DSL.nullif(subscriberCount, BigDecimal.ZERO)),
                BigDecimal.ZERO
        ).as("avgSales");

        // 평균 데이터 사용량 = 총데이터사용량 / 가입자수
        Field<BigDecimal> avgDataUsageGb = DSL.coalesce(
                totalDataUsage.div(DSL.nullif(subscriberCount, BigDecimal.ZERO)),
                BigDecimal.ZERO
        ).as("avgDataUsageGb");

        // 조인 경로
        // subscription -> member -> address
        //              -> product
        //              -> usage_monthly(해당 월만, left join)
        //
        return dsl
                .select(
                        ADDRESS.PROVINCE.as("province"),
                        avgSales,
                        avgDataUsageGb
                )
                .from(SUBSCRIPTION)
                .join(MEMBER).on(MEMBER.MEMBER_ID.eq(SUBSCRIPTION.MEMBER_ID))
                .join(ADDRESS).on(ADDRESS.ADDRESS_ID.eq(MEMBER.ADDRESS_ID))
                .join(PRODUCT).on(PRODUCT.PRODUCT_ID.eq(SUBSCRIPTION.PRODUCT_ID))
                .leftJoin(USAGE_MONTHLY).on(
                        USAGE_MONTHLY.SUBSCRIPTION_ID.eq(SUBSCRIPTION.SUBSCRIPTION_ID)
                                .and(USAGE_MONTHLY.YYYYMM.eq(yyyymm))
                )
                .where(SUBSCRIPTION.STATUS.isTrue())
                .groupBy(ADDRESS.PROVINCE)
                .fetch(this::toRawData);
    }

    // select 결과를 record DTO로 변환
    private RegionalMetricRawData toRawData(Record3<String, BigDecimal, BigDecimal> r) {
        return new RegionalMetricRawData(
                r.get("province", String.class),
                r.get("avgSales", BigDecimal.class),
                r.get("avgDataUsageGb", BigDecimal.class)
        );
    }
}
