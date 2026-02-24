package site.holliverse.admin.query.dao;

import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import site.holliverse.admin.query.jooq.enums.MemberMembershipType;
import site.holliverse.admin.query.jooq.enums.MemberRoleType;
import site.holliverse.admin.query.jooq.enums.MemberSignupType;
import site.holliverse.admin.query.jooq.enums.MemberStatusType;
import site.holliverse.admin.query.jooq.enums.ProductTypeEnum;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static site.holliverse.admin.query.jooq.tables.Address.ADDRESS;
import static site.holliverse.admin.query.jooq.tables.Member.MEMBER;
import static site.holliverse.admin.query.jooq.tables.Product.PRODUCT;
import static site.holliverse.admin.query.jooq.tables.Subscription.SUBSCRIPTION;
import static site.holliverse.admin.query.jooq.tables.UsageMonthly.USAGE_MONTHLY;

/**
 * AdminRegionalMetricDao 통합 테스트.
 *
 * 검증 포인트:
 * - 활성 구독(status=true)만 집계되는지
 * - 요청 월(yyyymm)에 해당하는 usage_monthly만 반영되는지
 * - 지역별 평균 매출/평균 데이터 사용량 계산이 기대값과 일치하는지
 */
@Disabled("CI 환경에서는 PostgreSQL DB 연결이 불가능하므로 임시 비활성화")
@ActiveProfiles("admin")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AdminRegionalMetricDao.class, AdminRegionalMetricDaoTest.TestConfig.class})
class AdminRegionalMetricDaoTest {

    @Autowired
    private AdminRegionalMetricDao adminRegionalMetricDao;

    @Autowired
    private DSLContext dsl;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DSLContext dslContext(DataSource dataSource) {
            return new DefaultDSLContext(dataSource, SQLDialect.POSTGRES);
        }
    }

    /**
     * 테스트 데이터 정리.
     * seed.metric prefix로 삽입한 데이터만 정리해 기존 데이터와 충돌을 최소화한다.
     */
    @BeforeEach
    void cleanupSeedData() {
        dsl.deleteFrom(USAGE_MONTHLY)
                .where(USAGE_MONTHLY.SUBSCRIPTION_ID.in(
                        dsl.select(SUBSCRIPTION.SUBSCRIPTION_ID)
                                .from(SUBSCRIPTION)
                                .where(SUBSCRIPTION.MEMBER_ID.in(
                                        dsl.select(MEMBER.MEMBER_ID)
                                                .from(MEMBER)
                                                .where(MEMBER.EMAIL.like("seed.metric.%@test.local"))
                                ))
                ))
                .execute();

        dsl.deleteFrom(SUBSCRIPTION)
                .where(SUBSCRIPTION.MEMBER_ID.in(
                        dsl.select(MEMBER.MEMBER_ID)
                                .from(MEMBER)
                                .where(MEMBER.EMAIL.like("seed.metric.%@test.local"))
                ))
                .execute();

        dsl.deleteFrom(MEMBER)
                .where(MEMBER.EMAIL.like("seed.metric.%@test.local"))
                .execute();

        dsl.deleteFrom(PRODUCT)
                .where(PRODUCT.PRODUCT_CODE.like("SEED_METRIC_%"))
                .execute();

        dsl.deleteFrom(ADDRESS)
                .where(ADDRESS.STREET_ADDRESS.like("seed-metric-%"))
                .execute();
    }

    @Test
    @DisplayName("지역 평균 매출/데이터 사용량은 활성 구독과 대상 월 데이터 기준으로 계산된다.")
    void findRegionalAverages_returns_expected_metrics() {
        String seoulProvince = "TEST_METRIC_SEOUL_" + System.nanoTime();
        String gyeonggiProvince = "TEST_METRIC_GYEONGGI_" + System.nanoTime();

        Long seoulAddressId = createAddress(seoulProvince, "강남구", "seed-metric-seoul");
        Long gyeonggiAddressId = createAddress(gyeonggiProvince, "성남시", "seed-metric-gyeonggi");

        Long p100 = createProduct("SEED_METRIC_P100", "P100", 100);
        Long p300 = createProduct("SEED_METRIC_P300", "P300", 300);
        Long p500 = createProduct("SEED_METRIC_P500", "P500", 500);
        Long p999 = createProduct("SEED_METRIC_P999", "P999", 999);

        Long sMember1 = createMember(seoulAddressId, "seed.metric.s1@test.local");
        Long sMember2 = createMember(seoulAddressId, "seed.metric.s2@test.local");
        Long gMember1 = createMember(gyeonggiAddressId, "seed.metric.g1@test.local");
        Long sInactive = createMember(seoulAddressId, "seed.metric.s3@test.local");

        Long subSeoul1 = createSubscription(sMember1, p100, true);
        Long subSeoul2 = createSubscription(sMember2, p300, true);
        Long subGyeonggi1 = createSubscription(gMember1, p500, true);
        Long subInactive = createSubscription(sInactive, p999, false);

        // 대상 월 데이터
        createUsage(subSeoul1, "202602", "10");
        createUsage(subSeoul2, "202602", "20");
        createUsage(subGyeonggi1, "202602", "40");

        // 다른 월 데이터(집계 제외)
        createUsage(subSeoul1, "202601", "999");

        // 비활성 구독 데이터(집계 제외)
        createUsage(subInactive, "202602", "999");

        List<RegionalMetricRawData> result = adminRegionalMetricDao.findRegionalAverages("202602");
        Map<String, RegionalMetricRawData> byProvince = result.stream()
                .collect(Collectors.toMap(RegionalMetricRawData::province, r -> r));

        assertThat(byProvince).containsKeys(seoulProvince, gyeonggiProvince);

        // 서울: 평균 매출 (100 + 300) / 2 = 200, 평균 데이터 (10 + 20) / 2 = 15
        assertThat(byProvince.get(seoulProvince).avgSales()).isEqualByComparingTo(new BigDecimal("200"));
        assertThat(byProvince.get(seoulProvince).avgDataUsageGb()).isEqualByComparingTo(new BigDecimal("15"));

        // 경기: 평균 매출 500, 평균 데이터 40
        assertThat(byProvince.get(gyeonggiProvince).avgSales()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(byProvince.get(gyeonggiProvince).avgDataUsageGb()).isEqualByComparingTo(new BigDecimal("40"));
    }

    /** 주소 테스트 데이터 생성 헬퍼 */
    private Long createAddress(String province, String city, String street) {
        return dsl.insertInto(ADDRESS)
                .set(ADDRESS.PROVINCE, province)
                .set(ADDRESS.CITY, city)
                .set(ADDRESS.STREET_ADDRESS, street)
                .set(ADDRESS.POSTAL_CODE, "00000")
                .returningResult(ADDRESS.ADDRESS_ID)
                .fetchSingle()
                .into(Long.class);
    }

    /** 상품 테스트 데이터 생성 헬퍼 */
    private Long createProduct(String code, String name, int salePrice) {
        return dsl.insertInto(PRODUCT)
                .set(PRODUCT.PRODUCT_CODE, code)
                .set(PRODUCT.NAME, name)
                .set(PRODUCT.PRICE, salePrice)
                .set(PRODUCT.SALE_PRICE, salePrice)
                .set(PRODUCT.PRODUCT_TYPE, ProductTypeEnum.MOBILE_PLAN)
                .set(PRODUCT.DISCOUNT_TYPE, "seed")
                .returningResult(PRODUCT.PRODUCT_ID)
                .fetchSingle()
                .into(Long.class);
    }

    /** 회원 테스트 데이터 생성 헬퍼 */
    private Long createMember(Long addressId, String email) {
        String phone = "010" + Math.abs(email.hashCode() % 100000000);
        if (phone.length() < 11) {
            phone = String.format("%-11s", phone).replace(' ', '0');
        }
        return dsl.insertInto(MEMBER)
                .set(MEMBER.ADDRESS_ID, addressId)
                .set(MEMBER.EMAIL, email)
                .set(MEMBER.PASSWORD, "seed-password")
                .set(MEMBER.NAME, "seed-user")
                .set(MEMBER.PHONE, phone)
                .set(MEMBER.BIRTH_DATE, LocalDate.of(1990, 1, 1))
                .set(MEMBER.GENDER, "M")
                .set(MEMBER.JOIN_DATE, LocalDate.now())
                .set(MEMBER.STATUS, MemberStatusType.ACTIVE)
                .set(MEMBER.TYPE, MemberSignupType.FORM)
                .set(MEMBER.ROLE, MemberRoleType.CUSTOMER)
                .set(MEMBER.MEMBERSHIP, MemberMembershipType.BASIC)
                .returningResult(MEMBER.MEMBER_ID)
                .fetchSingle()
                .into(Long.class);
    }

    /** 구독 테스트 데이터 생성 헬퍼 */
    private Long createSubscription(Long memberId, Long productId, boolean active) {
        return dsl.insertInto(SUBSCRIPTION)
                .set(SUBSCRIPTION.MEMBER_ID, memberId)
                .set(SUBSCRIPTION.PRODUCT_ID, productId)
                .set(SUBSCRIPTION.STATUS, active)
                .returningResult(SUBSCRIPTION.SUBSCRIPTION_ID)
                .fetchSingle()
                .into(Long.class);
    }

    /** 월별 사용량 테스트 데이터 생성 헬퍼 */
    private void createUsage(Long subscriptionId, String yyyymm, String dataGb) {
        dsl.insertInto(USAGE_MONTHLY)
                .set(USAGE_MONTHLY.SUBSCRIPTION_ID, subscriptionId)
                .set(USAGE_MONTHLY.YYYYMM, yyyymm)
                .set(USAGE_MONTHLY.USAGE_DETAILS, JSONB.valueOf("{\"data_gb\":\"" + dataGb + "\"}"))
                .execute();
    }
}