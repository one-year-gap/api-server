package site.holliverse.admin.web;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.admin.query.jooq.enums.MemberRoleType;
import site.holliverse.admin.query.jooq.enums.MemberSignupType;
import site.holliverse.admin.query.jooq.enums.MemberStatusType;
import site.holliverse.admin.query.jooq.enums.SupportStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static site.holliverse.admin.query.jooq.tables.Category.CATEGORY;
import static site.holliverse.admin.query.jooq.tables.CategoryGroup.CATEGORY_GROUP;
import static site.holliverse.admin.query.jooq.tables.Member.MEMBER;
import static site.holliverse.admin.query.jooq.tables.SupportCase.SUPPORT_CASE;

@ActiveProfiles({"admin", "test"})
@SpringBootTest
@AutoConfigureMockMvc
class CounselControllerIntegrationTest {

    private static final String TEST_CATEGORY_GROUP_CODE = "CG_TST_IT";
    private static final String TEST_CATEGORY_CODE = "TST_IT_001";
    private static final String TEST_TITLE_PREFIX = "counsel-it-";
    private static final String TEST_EMAIL_PREFIX = "counsel-it-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dsl;

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(SUPPORT_CASE)
                .where(SUPPORT_CASE.TITLE.like(TEST_TITLE_PREFIX + "%"))
                .execute();
        dsl.deleteFrom(MEMBER)
                .where(MEMBER.EMAIL.like(TEST_EMAIL_PREFIX + "%@example.com"))
                .execute();
        ensureCategory();
    }

    @Test
    @DisplayName("일 트래픽 통합 테스트: DB 데이터 기준으로 0~23시 응답이 내려온다.")
    void get_daily_traffic_integration() throws Exception {
        Long memberId = createDummyMember();
        LocalDate targetDate = LocalDate.of(2026, 2, 24);

        insertSupportCase(memberId, targetDate.atTime(0, 1), "d-0-a");
        insertSupportCase(memberId, targetDate.atTime(0, 59), "d-0-b");
        insertSupportCase(memberId, targetDate.atTime(1, 10), "d-1");
        insertSupportCase(memberId, targetDate.atTime(23, 59), "d-23");
        insertSupportCase(memberId, targetDate.plusDays(1).atTime(0, 0), "d-out");

        mockMvc.perform(get("/api/v1/admin/counsel-traffic/daily")
                        .param("date", "2026-02-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.maxCount").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(24))
                .andExpect(jsonPath("$.data.items[0].hour").value(0))
                .andExpect(jsonPath("$.data.items[0].count").value(2))
                .andExpect(jsonPath("$.data.items[1].hour").value(1))
                .andExpect(jsonPath("$.data.items[1].count").value(1))
                .andExpect(jsonPath("$.data.items[23].hour").value(23))
                .andExpect(jsonPath("$.data.items[23].count").value(1));
    }

    @Test
    @DisplayName("월 트래픽 통합 테스트: DB 데이터 기준으로 1~말일 응답이 내려온다.")
    void get_monthly_traffic_integration() throws Exception {
        Long memberId = createDummyMember();

        insertSupportCase(memberId, LocalDateTime.of(2026, 2, 1, 0, 1), "m-1-a");
        insertSupportCase(memberId, LocalDateTime.of(2026, 2, 1, 9, 0), "m-1-b");
        insertSupportCase(memberId, LocalDateTime.of(2026, 2, 15, 15, 0), "m-15");
        insertSupportCase(memberId, LocalDateTime.of(2026, 2, 28, 23, 59), "m-28");
        insertSupportCase(memberId, LocalDateTime.of(2026, 3, 1, 0, 0), "m-out");

        mockMvc.perform(get("/api/v1/admin/counsel-traffic/monthly")
                        .param("month", "2026-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.maxCount").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(28))
                .andExpect(jsonPath("$.data.items[0].day").value(1))
                .andExpect(jsonPath("$.data.items[0].count").value(2))
                .andExpect(jsonPath("$.data.items[14].day").value(15))
                .andExpect(jsonPath("$.data.items[14].count").value(1))
                .andExpect(jsonPath("$.data.items[27].day").value(28))
                .andExpect(jsonPath("$.data.items[27].count").value(1));
    }

    @Test
    @DisplayName("일 트래픽 통합 테스트: 잘못된 date 형식이면 400 에러를 반환한다.")
    void get_daily_traffic_invalid_date() throws Exception {
        mockMvc.perform(get("/api/v1/admin/counsel-traffic/daily")
                        .param("date", "2026-02-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorDetail.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errorDetail.field").value("date"));
    }

    @Test
    @DisplayName("월 트래픽 통합 테스트: 잘못된 month 형식이면 400 에러를 반환한다.")
    void get_monthly_traffic_invalid_month() throws Exception {
        mockMvc.perform(get("/api/v1/admin/counsel-traffic/monthly")
                        .param("month", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorDetail.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errorDetail.field").value("month"));
    }

    private Long createDummyMember() {
        String suffix = String.valueOf(System.nanoTime());

        return dsl.insertInto(MEMBER)
                .set(MEMBER.PROVIDER_ID, "provider-it-" + suffix)
                .set(MEMBER.EMAIL, TEST_EMAIL_PREFIX + suffix + "@example.com")
                .set(MEMBER.NAME, "통합테스트회원-" + suffix)
                .set(MEMBER.STATUS, MemberStatusType.ACTIVE)
                .set(MEMBER.TYPE, MemberSignupType.GOOGLE)
                .set(MEMBER.ROLE, MemberRoleType.CUSTOMER)
                .set(MEMBER.JOIN_DATE, LocalDate.of(2026, 1, 1))
                .returningResult(MEMBER.MEMBER_ID)
                .fetchSingle()
                .into(Long.class);
    }

    private void ensureCategory() {
        dsl.insertInto(CATEGORY_GROUP)
                .set(CATEGORY_GROUP.CATEGORY_GROUP_CODE, TEST_CATEGORY_GROUP_CODE)
                .set(CATEGORY_GROUP.CATEGORY_NAME, "통합테스트 대분류")
                .onConflict(CATEGORY_GROUP.CATEGORY_GROUP_CODE)
                .doNothing()
                .execute();

        dsl.insertInto(CATEGORY)
                .set(CATEGORY.CATEGORY_CODE, TEST_CATEGORY_CODE)
                .set(CATEGORY.CATEGORY_GROUP_CODE, TEST_CATEGORY_GROUP_CODE)
                .set(CATEGORY.CATEGORY_NAME, "통합테스트 소분류")
                .onConflict(CATEGORY.CATEGORY_CODE)
                .doNothing()
                .execute();
    }

    private void insertSupportCase(Long memberId, LocalDateTime createdAt, String suffix) {
        dsl.insertInto(SUPPORT_CASE)
                .set(SUPPORT_CASE.MEMBER_ID, memberId)
                .set(SUPPORT_CASE.CATEGORY_CODE, TEST_CATEGORY_CODE)
                .set(SUPPORT_CASE.STATUS, SupportStatus.OPEN)
                .set(SUPPORT_CASE.TITLE, TEST_TITLE_PREFIX + suffix)
                .set(SUPPORT_CASE.QUESTION_TEXT, "integration-test-question")
                .set(SUPPORT_CASE.CREATED_AT, createdAt)
                .set(SUPPORT_CASE.UPDATED_AT, createdAt)
                .execute();
    }
}
