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
import site.holliverse.infra.error.InfraErrorCode;
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
@Disabled("Run locally with PostgreSQL.")
class MemberControllerIntegrationTest {

    private static final String TEST_EMAIL_PREFIX = "member-it-";
    private static final String TEST_PRODUCT_CODE_PREFIX = "IT-MOBILE-";
    private static final String TEST_STREET_PREFIX = "it-street-";
    private static final String API_ME = "/api/v1/customer/me";
    private static final String USAGE_YYYYMM = "202603";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EncryptionTool encryptionTool;

    @Test
    @DisplayName("GET /api/v1/customer/me returns profile fields including contract info")
    void getMyProfile_success() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        MemberFixtureResult fixture = createMemberFixture(suffix, true);
        setSecurityContextWithMember(fixture.memberId());

        try {
            mockMvc.perform(get(API_ME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.name").value("Hong Gil-dong"))
                    .andExpect(jsonPath("$.data.membership").value("GOLD"))
                    .andExpect(jsonPath("$.data.email").value(fixture.email()))
                    .andExpect(jsonPath("$.data.phone").value(fixture.phone()))
                    .andExpect(jsonPath("$.data.address").value("Seoul Gangnam " + TEST_STREET_PREFIX + suffix))
                    .andExpect(jsonPath("$.data.birthDate").value("2000-01-01"))
                    .andExpect(jsonPath("$.data.contract.contractStartDate").value("2026-03-18"))
                    .andExpect(jsonPath("$.data.contract.contractEndDate").value("2028-03-18"))
                    .andExpect(jsonPath("$.data.contract.contractMonths").value(24))
                    .andExpect(jsonPath("$.data.subscriptions[0].productType").value("MOBILE_PLAN"))
                    .andExpect(jsonPath("$.data.mobilePlan.dataAmount").value("Unlimited"))
                    .andExpect(jsonPath("$.data.mobilePlan.usageDetails.dataGb").value(2.8))
                    .andExpect(jsonPath("$.data.mobilePlan.usageDetails.smsCnt").value(54))
                    .andExpect(jsonPath("$.data.mobilePlan.usageDetails.voiceMin").value(145));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("GET /api/v1/customer/me returns DECRYPTION_FAILED when name decryption fails")
    void getMyProfile_decryptionFailed() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        MemberFixtureResult fixture = createMemberFixture(suffix, false);
        setSecurityContextWithMember(fixture.memberId());

        try {
            mockMvc.perform(get(API_ME))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorDetail.code").value(InfraErrorCode.DECRYPTION_FAILED.code()))
                    .andExpect(jsonPath("$.errorDetail.field").isEmpty());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private MemberFixtureResult createMemberFixture(String suffix, boolean encryptName) {
        Long addressId = jdbcTemplate.queryForObject(
                "INSERT INTO address (province, city, street_address, postal_code) VALUES (?, ?, ?, ?) RETURNING address_id",
                Long.class,
                "Seoul",
                "Gangnam",
                TEST_STREET_PREFIX + suffix,
                "12345"
        );

        String plainName = "Hong Gil-dong";
        String encryptedName = encryptName ? encryptionTool.encrypt(plainName) : plainName;
        String phonePlain = "010" + String.format("%08d", Math.abs(Long.parseLong(suffix)) % 100_000_000);
        String encryptedPhone = encryptionTool.encrypt(phonePlain);
        String email = TEST_EMAIL_PREFIX + suffix + "@example.com";

        Long memberId = jdbcTemplate.queryForObject(
                "INSERT INTO member (" +
                        "address_id, provider_id, email, password, name, phone, birth_date, gender, join_date, " +
                        "status, type, role, membership" +
                        ") VALUES (" +
                        "?, ?, ?, ?, ?, ?, DATE '2000-01-01', ?, CURRENT_DATE, " +
                        "?::member_status_type, ?::member_signup_type, ?::member_role_type, ?::member_membership_type" +
                        ") RETURNING member_id",
                Long.class,
                addressId,
                null,
                email,
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
                "5G Premium Plan",
                74000,
                62000,
                "MOBILE_PLAN",
                "Selective 25%",
                "[\"data\"]"
        );

        jdbcTemplate.update(
                "INSERT INTO mobile_plan (" +
                        "product_id, data_amount, tethering_sharing_data, benefit_brands, " +
                        "benefit_voice_call, benefit_sms, benefit_media, benefit_premium, benefit_signature_family_discount" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                productId,
                "Unlimited",
                80,
                "Brand benefit",
                "Unlimited",
                "Basic",
                null,
                null,
                null
        );

        Long subscriptionId = jdbcTemplate.queryForObject(
                "INSERT INTO subscription (member_id, product_id, start_date, contract_months, contract_end_date, status) " +
                        "VALUES (?, ?, TIMESTAMP '2026-03-18 00:00:00', ?, TIMESTAMP '2028-03-18 00:00:00', true) RETURNING subscription_id",
                Long.class,
                memberId,
                productId,
                24
        );

        jdbcTemplate.update(
                "INSERT INTO usage_monthly (subscription_id, yyyymm, usage_details) VALUES (?, ?, ?::jsonb)",
                subscriptionId,
                USAGE_YYYYMM,
                "{\"data_gb\": 2.8, \"sms_cnt\": 54, \"voice_min\": 145}"
        );

        return new MemberFixtureResult(memberId, email, phonePlain);
    }

    private record MemberFixtureResult(long memberId, String email, String phone) {}

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
