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

    private final DSLContext dsl;

    /*
     * 지역별 월간 지표를 조회한다.
     *
     * 지표 정의(현재 비즈니스 기준):
     * - avgSales: 지역 매출 총합 / 지역 유니크 가입자 수
     * - avgDataUsageGb: 지역 데이터 사용량 총합(GB) / 지역 유니크 가입자 수
     */
    public List<RegionalMetricRawData> findRegionalAverages(String yyyymm) {
        // 지역 내 활성 구독의 월 매출 총합 분자
        Field<BigDecimal> totalRevenue = DSL.sum(PRODUCT.SALE_PRICE.cast(BigDecimal.class));

        // usage_details(JSONB)에서 data_gb 값을 GB 단위 numeric으로 추출
        Field<BigDecimal> dataUsageGb = DSL.field(
                "NULLIF({0} ->> 'data_gb', '')::numeric",
                BigDecimal.class,
                USAGE_MONTHLY.USAGE_DETAILS
        );

        // 지역 내 월 데이터 사용량 총합 분자
        // - 데이터가 없으면 0으로 간주
        Field<BigDecimal> totalDataUsage = DSL.sum(DSL.coalesce(dataUsageGb, BigDecimal.ZERO));

        // 지역 내 유니크 가입자 수 분모
        // - 구독 기준이 아니라 member_id distinct 기준
        Field<BigDecimal> subscriberCount = DSL.countDistinct(MEMBER.MEMBER_ID).cast(BigDecimal.class);

        // 평균 매출(ARPU 성격): totalRevenue / subscriberCount
        // - 0으로 나누는 경우를 막기 위해 nullif + coalesce 사용
        Field<BigDecimal> avgSales = DSL.coalesce(
                totalRevenue.div(DSL.nullif(subscriberCount, BigDecimal.ZERO)),
                BigDecimal.ZERO
        ).as("avgSales");

        // 평균 데이터 사용량: totalDataUsage / subscriberCount
        Field<BigDecimal> avgDataUsageGb = DSL.coalesce(
                totalDataUsage.div(DSL.nullif(subscriberCount, BigDecimal.ZERO)),
                BigDecimal.ZERO
        ).as("avgDataUsageGb");

        /*
         * 조인 경로:
         * subscription -> member -> address(지역) -> product(요금)
         *                                \-> usage_monthly(해당 yyyymm만)
         *
         * 필터:
         * - subscription.status = true (활성 구독만)
         * - usage_monthly는 left join + yyyymm 조건 (해당 월 사용량 없으면 0 처리)
         */
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

    // select alias(province, avgSales, avgDataUsageGb)를 record DTO로 매핑
    private RegionalMetricRawData toRawData(Record3<String, BigDecimal, BigDecimal> r) {
        return new RegionalMetricRawData(
                r.get("province", String.class),
                r.get("avgSales", BigDecimal.class),
                r.get("avgDataUsageGb", BigDecimal.class)
        );
    }
}
