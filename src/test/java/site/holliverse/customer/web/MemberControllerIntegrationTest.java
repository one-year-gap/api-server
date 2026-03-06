package site.holliverse.customer.web;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.security.CustomUserDetails;
import site.holliverse.shared.util.EncryptionTool;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"customer", "test"})
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Disabled("CI 환경에 PostgreSQL 연결 불가(db_migrator 인증 실패), 로컬에서만 수동 실행")
class MemberControllerIntegrationTest {

    private static final String TEST_EMAIL_PREFIX = "member-it-";
    private static final String TEST_PRODUCT_CODE_PREFIX = "IT-MOBILE-";
    private static final String TEST_STREET_PREFIX = "it-street-";
    private static final String API_ME = "/api/v1/customer/me";
    private static final String USAGE_YYYYMM = "202603";

    private static final String JSON_STATUS = "$.status";
    private static final String JSON_DATA_NAME = "$.data.name";
    private static final String JSON_DATA_PHONE = "$.data.phone";
    private static final String JSON_DATA_MEMBERSHIP = "$.data.membership";
    private static final String JSON_FIRST_SUBSCRIPTION_TYPE = "$.data.subscriptions[0].productType";
    private static final String JSON_MOBILE_DATA_AMOUNT = "$.data.mobilePlan.dataAmount";
    private static final String JSON_USAGE_DATA_GB = "$.data.mobilePlan.usageDetails.dataGb";
    private static final String JSON_USAGE_SMS_CNT = "$.data.mobilePlan.usageDetails.smsCnt";
    private static final String JSON_USAGE_VOICE_MIN = "$.data.mobilePlan.usageDetails.voiceMin";
    private static final String JSON_ERROR_CODE = "$.errorDetail.code";
    private static final String JSON_ERROR_FIELD = "$.errorDetail.field";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EncryptionTool encryptionTool;

    @Test
    @DisplayName("GET /api/v1/customer/me - DB 데이터 기준으로 회원정보/구독/모바일 사용량을 반환한다.")
    void getMyProfile_success() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        MemberFixtureResult fixture = createMemberFixture(suffix, true);
        setSecurityContextWithMember(fixture.memberId());

        try {
            mockMvc.perform(get(API_ME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_STATUS).value("success"))
                    .andExpect(jsonPath(JSON_DATA_NAME).value("홍길동"))
                    .andExpect(jsonPath(JSON_DATA_PHONE).value(fixture.expectedMaskedPhone()))
                    .andExpect(jsonPath(JSON_DATA_MEMBERSHIP).value("GOLD"))
                    .andExpect(jsonPath(JSON_FIRST_SUBSCRIPTION_TYPE).value("MOBILE_PLAN"))
                    .andExpect(jsonPath(JSON_MOBILE_DATA_AMOUNT).value("완전 무제한"))
                    .andExpect(jsonPath(JSON_USAGE_DATA_GB).value(2.8))
                    .andExpect(jsonPath(JSON_USAGE_SMS_CNT).value(54))
                    .andExpect(jsonPath(JSON_USAGE_VOICE_MIN).value(145));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("GET /api/v1/customer/me - 이름 복호화 실패 시 DECRYPTION_FAILED(500)를 반환한다.")
    void getMyProfile_decryptionFailed() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        MemberFixtureResult fixture = createMemberFixture(suffix, false);
        setSecurityContextWithMember(fixture.memberId());

        try {
            mockMvc.perform(get(API_ME))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(JSON_ERROR_CODE).value("DECRYPTION_FAILED"))
                    .andExpect(jsonPath(JSON_ERROR_FIELD).value("name"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // --- Helpers ---
    private MemberFixtureResult createMemberFixture(String suffix, boolean encryptName) {
        Long addressId = jdbcTemplate.queryForObject(
                "INSERT INTO address (province, city, street_address, postal_code) " +
                        "VALUES (?, ?, ?, ?) RETURNING address_id",
                Long.class,
                "it-province",
                "it-city",
                TEST_STREET_PREFIX + suffix,
                "12345"
        );

        String encryptedName = encryptName ? encryptionTool.encrypt("홍길동") : "홍길동";
        String phonePlain = "010" + String.format("%08d", Math.abs(Long.parseLong(suffix)) % 100_000_000);
        String encryptedPhone = encryptionTool.encrypt(phonePlain);
        String expectedMaskedPhone = "010-****-" + phonePlain.substring(phonePlain.length() - 4);

        Long memberId = jdbcTemplate.queryForObject(
                "INSERT INTO member (" +
                        "address_id, provider_id, email, password, name, phone, birth_date, gender, join_date, " +
                        "status, type, role, membership" +
                        ") VALUES (" +
                        "?, ?, ?, ?, ?, ?, CURRENT_DATE, ?, CURRENT_DATE, " +
                        "?::member_status_type, ?::member_signup_type, ?::member_role_type, ?::member_membership_type" +
                        ") RETURNING member_id",
                Long.class,
                addressId,
                null,
                TEST_EMAIL_PREFIX + suffix + "@example.com",
                "test-password",
                encryptedName,
                encryptedPhone,
                "M",
                "ACTIVE",
                "FORM",
                "CUSTOMER",
                "GOLD"
        );

        Long productId = jdbcTemplate.queryForObject(
                "INSERT INTO product (" +
                        "product_code, name, price, sale_price, product_type, discount_type, tags" +
                        ") VALUES (" +
                        "?, ?, ?, ?, ?::product_type_enum, ?, ?::jsonb" +
                        ") RETURNING product_id",
                Long.class,
                TEST_PRODUCT_CODE_PREFIX + suffix,
                "5G 프리미어 플러스",
                74000,
                62000,
                "MOBILE_PLAN",
                "선택약정 25%",
                "[\"데이터\"]"
        );

        jdbcTemplate.update(
                "INSERT INTO mobile_plan (" +
                        "product_id, data_amount, tethering_sharing_data, benefit_brands, " +
                        "benefit_voice_call, benefit_sms, benefit_media, benefit_premium, benefit_signature_family_discount" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                productId,
                "완전 무제한",
                80,
                "브랜드 혜택",
                "무제한",
                "기본 제공",
                null,
                null,
                null
        );

        Long subscriptionId = jdbcTemplate.queryForObject(
                "INSERT INTO subscription (member_id, product_id, start_date, status) " +
                        "VALUES (?, ?, NOW(), true) RETURNING subscription_id",
                Long.class,
                memberId,
                productId
        );

        jdbcTemplate.update(
                "INSERT INTO usage_monthly (subscription_id, yyyymm, usage_details) VALUES (?, ?, ?::jsonb)",
                subscriptionId,
                USAGE_YYYYMM,
                "{\"data_gb\": 2.8, \"sms_cnt\": 54, \"voice_min\": 145}"
        );

        return new MemberFixtureResult(memberId, expectedMaskedPhone);
    }

    private record MemberFixtureResult(long memberId, String expectedMaskedPhone) {}

    // 보안 context mocking
    private void setSecurityContextWithMember(Long memberId) {
        Authentication authentication = authenticationWithMemberId(memberId);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private Authentication authenticationWithMemberId(Long memberId) {
        CustomUserDetails user = new CustomUserDetails(
                memberId,
                TEST_EMAIL_PREFIX + "auth@example.com",
                null,
                "CUSTOMER",
                MemberStatus.ACTIVE
        );
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
