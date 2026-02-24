package site.holliverse.admin.query.dao;

import org.jooq.DSLContext;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static site.holliverse.admin.query.jooq.tables.Address.ADDRESS;
import static site.holliverse.admin.query.jooq.tables.Member.MEMBER;
import static site.holliverse.admin.query.jooq.tables.Product.PRODUCT;
import static site.holliverse.admin.query.jooq.tables.Subscription.SUBSCRIPTION;


@Disabled("CI 환경에서는 PostgreSQL DB 연결이 불가능하므로 임시 비활성화")
@ActiveProfiles("admin")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AdminRegionalTopPlanDao.class, AdminRegionalTopPlanDaoTest.TestConfig.class})
class AdminRegionalTopPlanDaoTest {

    @Autowired
    private AdminRegionalTopPlanDao adminRegionalTopPlanDao;

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
     * 테스트용 데이터 정리.
     *
     * - 이메일 prefix: seed.topplan.
     * - 상품 코드 prefix: SEED_TOPPLAN_
     * - 주소 street prefix: seed-topplan-
     *
     * 실행 순서상 FK 제약을 피하기 위해 subscription -> member -> product -> address 순으로 삭제한다.
     */
    @BeforeEach
    void cleanupSeedData() {
        dsl.deleteFrom(SUBSCRIPTION)
                .where(SUBSCRIPTION.MEMBER_ID.in(
                        dsl.select(MEMBER.MEMBER_ID)
                                .from(MEMBER)
                                .where(MEMBER.EMAIL.like("seed.topplan.%@test.local"))
                ))
                .execute();

        dsl.deleteFrom(MEMBER)
                .where(MEMBER.EMAIL.like("seed.topplan.%@test.local"))
                .execute();

        dsl.deleteFrom(PRODUCT)
                .where(PRODUCT.PRODUCT_CODE.like("SEED_TOPPLAN_%"))
                .execute();

        dsl.deleteFrom(ADDRESS)
                .where(ADDRESS.STREET_ADDRESS.like("seed-topplan-%"))
                .execute();
    }

    /**
     * 기대 결과:
     * - 서울(테스트 전용 province) 결과가 [ALPHA_PLAN, BETA_PLAN, GAMMA_PLAN] 순서여야 한다.
     *   (ALPHA/BETA는 가입자 수 동률이므로 요금제명 오름차순 tie-break)
     * - 경기(테스트 전용 province) 결과는 [DELTA_PLAN] 1건이어야 한다.
     */
    @Test
    @DisplayName("지역별 Top N 요금제가 가입자 수/요금제명 정렬 기준으로 반환된다.")
    void findTopPlansByAllProvinces_returns_ranked_top_plans() {
        // 기존 운영 데이터와 충돌하지 않도록 province를 테스트마다 고유값으로 생성
        String seoulProvince = "TEST_TOPPLAN_SEOUL_" + System.nanoTime();
        String gyeonggiProvince = "TEST_TOPPLAN_GYEONGGI_" + System.nanoTime();

        Long seoulAddressId = createAddress(seoulProvince, "강남구", "seed-topplan-seoul");
        Long gyeonggiAddressId = createAddress(gyeonggiProvince, "성남시", "seed-topplan-gyeonggi");

        Long alphaPlan = createProduct("SEED_TOPPLAN_A", "ALPHA_PLAN");
        Long betaPlan = createProduct("SEED_TOPPLAN_B", "BETA_PLAN");
        Long gammaPlan = createProduct("SEED_TOPPLAN_C", "GAMMA_PLAN");
        Long deltaPlan = createProduct("SEED_TOPPLAN_D", "DELTA_PLAN");

        // 서울: ALPHA 2명, BETA 2명(동률), GAMMA 1명
        createActiveSubscription(createMember(seoulAddressId, "seed.topplan.s1@test.local"), alphaPlan);
        createActiveSubscription(createMember(seoulAddressId, "seed.topplan.s2@test.local"), alphaPlan);
        createActiveSubscription(createMember(seoulAddressId, "seed.topplan.s3@test.local"), betaPlan);
        createActiveSubscription(createMember(seoulAddressId, "seed.topplan.s4@test.local"), betaPlan);
        createActiveSubscription(createMember(seoulAddressId, "seed.topplan.s5@test.local"), gammaPlan);

        // 경기: DELTA 1명
        createActiveSubscription(createMember(gyeonggiAddressId, "seed.topplan.g1@test.local"), deltaPlan);

        List<RegionalTopPlanRawData> result = adminRegionalTopPlanDao.findTopPlansByAllProvinces(3);

        Map<String, List<String>> plansByProvince = result.stream()
                .collect(Collectors.groupingBy(
                        RegionalTopPlanRawData::province,
                        Collectors.mapping(RegionalTopPlanRawData::planName, Collectors.toList())
                ));

        assertThat(plansByProvince.get(seoulProvince))
                .containsExactly("ALPHA_PLAN", "BETA_PLAN", "GAMMA_PLAN");
        assertThat(plansByProvince.get(gyeonggiProvince))
                .containsExactly("DELTA_PLAN");
    }

    /**
     * 기대 결과:
     * - 서울(테스트 전용 province): 활성 2건 + 비활성 1건 중 활성만 집계되어 2
     * - 경기(테스트 전용 province): 활성 1건 집계되어 1
     */
    @Test
    @DisplayName("지역별 가입자 수 집계 시 비활성 구독은 제외된다.")
    void findSubscriberCountsByAllProvinces_excludes_inactive_subscriptions() {
        String seoulProvince = "TEST_TOPPLAN_COUNT_SEOUL_" + System.nanoTime();
        String gyeonggiProvince = "TEST_TOPPLAN_COUNT_GYEONGGI_" + System.nanoTime();

        Long seoulAddressId = createAddress(seoulProvince, "강남구", "seed-topplan-count-seoul");
        Long gyeonggiAddressId = createAddress(gyeonggiProvince, "성남시", "seed-topplan-count-gyeonggi");

        Long commonPlan = createProduct("SEED_TOPPLAN_Z", "COMMON_PLAN");

        // 서울: 활성 2 + 비활성 1
        createActiveSubscription(createMember(seoulAddressId, "seed.topplan.c1@test.local"), commonPlan);
        createActiveSubscription(createMember(seoulAddressId, "seed.topplan.c2@test.local"), commonPlan);
        createInactiveSubscription(createMember(seoulAddressId, "seed.topplan.c3@test.local"), commonPlan);

        // 경기: 활성 1
        createActiveSubscription(createMember(gyeonggiAddressId, "seed.topplan.c4@test.local"), commonPlan);

        List<RegionalSubscriberCountRawData> result = adminRegionalTopPlanDao.findSubscriberCountsByAllProvinces();
        Map<String, Long> countByProvince = result.stream()
                .collect(Collectors.toMap(RegionalSubscriberCountRawData::province, RegionalSubscriberCountRawData::regionalSubscriberCount));

        assertThat(countByProvince.get(seoulProvince)).isEqualTo(2L);
        assertThat(countByProvince.get(gyeonggiProvince)).isEqualTo(1L);
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
    private Long createProduct(String productCode, String name) {
        return dsl.insertInto(PRODUCT)
                .set(PRODUCT.PRODUCT_CODE, productCode)
                .set(PRODUCT.NAME, name)
                .set(PRODUCT.PRICE, 50000)
                .set(PRODUCT.SALE_PRICE, 45000)
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

    /** 활성 구독 생성 헬퍼 */
    private void createActiveSubscription(Long memberId, Long productId) {
        dsl.insertInto(SUBSCRIPTION)
                .set(SUBSCRIPTION.MEMBER_ID, memberId)
                .set(SUBSCRIPTION.PRODUCT_ID, productId)
                .set(SUBSCRIPTION.STATUS, true)
                .execute();
    }

    /** 비활성 구독 생성 헬퍼 */
    private void createInactiveSubscription(Long memberId, Long productId) {
        dsl.insertInto(SUBSCRIPTION)
                .set(SUBSCRIPTION.MEMBER_ID, memberId)
                .set(SUBSCRIPTION.PRODUCT_ID, productId)
                .set(SUBSCRIPTION.STATUS, false)
                .execute();
    }
}