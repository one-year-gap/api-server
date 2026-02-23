package site.holliverse.admin.query.dao;

import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static site.holliverse.admin.query.jooq.Tables.ADDRESS;
import static site.holliverse.admin.query.jooq.Tables.MEMBER;
import static site.holliverse.admin.query.jooq.Tables.PRODUCT;
import static site.holliverse.admin.query.jooq.Tables.SUBSCRIPTION;
import static site.holliverse.admin.query.jooq.Tables.USAGE_MONTHLY;

//@Disabled("CI 환경에서는 PostgreSQL DB 연결이 불가해 로컬에서만 실행")
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

    @Test
    @DisplayName("월 기준 지역 평균 매출/데이터 사용량(GB)을 계산한다.")
    void findRegionalAverages_success() {
        String yyyymm = "202601";

        Long seoulAddressId = createAddress("서울", "강남구");
        Long busanAddressId = createAddress("부산광역시", "해운대구");

        Long seoulMember1 = createMember(seoulAddressId, "seoul1");
        Long seoulMember2 = createMember(seoulAddressId, "seoul2");
        Long busanMember1 = createMember(busanAddressId, "busan1");

        Long product1 = createProduct("P_SEOUL_1", 10_000);
        Long product2 = createProduct("P_SEOUL_2", 30_000);
        Long product3 = createProduct("P_BUSAN_1", 50_000);

        Long sub1 = createSubscription(seoulMember1, product1);
        Long sub2 = createSubscription(seoulMember2, product2);
        createSubscription(busanMember1, product3);

        createUsage(sub1, yyyymm, 10);
        createUsage(sub2, yyyymm, 20);

        // 다른 월 데이터는 집계에서 제외되어야 한다.
        createUsage(sub1, "202512", 999);

        List<RegionalMetricRawData> result = adminRegionalMetricDao.findRegionalAverages(yyyymm);
        Map<String, RegionalMetricRawData> byProvince = result.stream()
                .collect(Collectors.toMap(RegionalMetricRawData::province, Function.identity()));

        RegionalMetricRawData seoul = byProvince.get("서울");
        assertThat(seoul).isNotNull();
        assertThat(seoul.avgSales()).isEqualByComparingTo(BigDecimal.valueOf(20_000));
        assertThat(seoul.avgDataUsageGb()).isEqualByComparingTo(BigDecimal.valueOf(15));

        RegionalMetricRawData busan = byProvince.get("부산광역시");
        assertThat(busan).isNotNull();
        assertThat(busan.avgSales()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
        assertThat(busan.avgDataUsageGb()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Long createAddress(String province, String city) {
        return dsl.insertInto(ADDRESS)
                .set(ADDRESS.PROVINCE, province)
                .set(ADDRESS.CITY, city)
                .set(ADDRESS.STREET_ADDRESS, "테스트로 " + System.nanoTime())
                .set(ADDRESS.POSTAL_CODE, "12345")
                .returningResult(ADDRESS.ADDRESS_ID)
                .fetchSingle()
                .into(Long.class);
    }

    private Long createMember(Long addressId, String uniquePrefix) {
        long now = System.nanoTime();
        return dsl.insertInto(MEMBER)
                .set(MEMBER.ADDRESS_ID, addressId)
                .set(MEMBER.PROVIDER_ID, (String) null)
                .set(MEMBER.EMAIL, uniquePrefix + now + "@test.com")
                .set(MEMBER.PASSWORD, "password123")
                .set(MEMBER.NAME, "테스터")
                .set(MEMBER.PHONE, "010" + String.valueOf(now).substring(0, 8))
                .set(MEMBER.BIRTH_DATE, LocalDate.of(1999, 1, 1))
                .set(MEMBER.GENDER, "M")
                .set(MEMBER.STATUS, MemberStatusType.ACTIVE)
                .set(MEMBER.TYPE, MemberSignupType.FORM)
                .set(MEMBER.ROLE, MemberRoleType.CUSTOMER)
                .set(MEMBER.MEMBERSHIP, MemberMembershipType.VIP)
                .returningResult(MEMBER.MEMBER_ID)
                .fetchSingle()
                .into(Long.class);
    }

    private Long createProduct(String codePrefix, int salePrice) {
        long now = System.nanoTime();
        return dsl.insertInto(PRODUCT)
                .set(PRODUCT.PRODUCT_CODE, codePrefix + "_" + now)
                .set(PRODUCT.NAME, "상품-" + now)
                .set(PRODUCT.PRICE, salePrice)
                .set(PRODUCT.SALE_PRICE, salePrice)
                .set(PRODUCT.PRODUCT_TYPE, ProductTypeEnum.MOBILE_PLAN)
                .set(PRODUCT.DISCOUNT_TYPE, "테스트")
                .returningResult(PRODUCT.PRODUCT_ID)
                .fetchSingle()
                .into(Long.class);
    }

    private Long createSubscription(Long memberId, Long productId) {
        return dsl.insertInto(SUBSCRIPTION)
                .set(SUBSCRIPTION.MEMBER_ID, memberId)
                .set(SUBSCRIPTION.PRODUCT_ID, productId)
                .set(SUBSCRIPTION.STATUS, true)
                .returningResult(SUBSCRIPTION.SUBSCRIPTION_ID)
                .fetchSingle()
                .into(Long.class);
    }

    private void createUsage(Long subscriptionId, String yyyymm, int dataGb) {
        dsl.insertInto(USAGE_MONTHLY)
                .set(USAGE_MONTHLY.SUBSCRIPTION_ID, subscriptionId)
                .set(USAGE_MONTHLY.YYYYMM, yyyymm)
                .set(USAGE_MONTHLY.USAGE_DETAILS, JSONB.jsonb("{\"data_gb\": " + dataGb + "}"))
                .execute();
    }
}
