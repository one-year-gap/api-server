package site.holliverse.admin.query.dao;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import site.holliverse.admin.query.jooq.enums.MemberRoleType;
import site.holliverse.admin.query.jooq.enums.MemberSignupType;
import site.holliverse.admin.query.jooq.enums.MemberStatusType;
import site.holliverse.admin.query.jooq.enums.SupportStatus;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static site.holliverse.admin.query.jooq.tables.Category.CATEGORY;
import static site.holliverse.admin.query.jooq.tables.CategoryGroup.CATEGORY_GROUP;
import static site.holliverse.admin.query.jooq.tables.Member.MEMBER;
import static site.holliverse.admin.query.jooq.tables.SupportCase.SUPPORT_CASE;

@ActiveProfiles({"admin", "test"})
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CounselDaoTest {

    private static final String TEST_CATEGORY_GROUP_CODE = "CG_TST";
    private static final String TEST_CATEGORY_CODE = "TST_001";
    private static final String TEST_TITLE_PREFIX = "counsel-dao-test-";
    private static final String TEST_EMAIL_PREFIX = "counsel-dao-test-";

    @Autowired
    private CounselDao repository;

    @Autowired
    private DSLContext dsl;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DSLContext dslContext(DataSource dataSource) {
            return new DefaultDSLContext(dataSource, SQLDialect.POSTGRES);
        }
    }

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

    @Nested
    @DisplayName("경계값 테스트")
    class BoundTest {

        @Test
        @DisplayName("시간 단위 조회 시 같은 날짜의 시간대별 건수를 집계한다.")
        void get_counsel_traffic_by_hour() {
            //given
            Long memberId = createDummyMember();
            LocalDate targetDate = LocalDate.of(2026, 3, 14);
            LocalDate unTargetDate = LocalDate.of(2026,3,15);

            insertSupportCase(memberId, targetDate.atTime(0, 0,0,0), "a");//3월 14일 0시로 집계되어야 함.
            insertSupportCase(memberId, targetDate.atTime(0, 0,0,1), "b");//3월 14일 0시로 집계되어야 함.
            insertSupportCase(memberId, targetDate.atTime(23, 59,59,59), "c");//3월 14일 23시로 집계되어야 함.
            insertSupportCase(memberId, unTargetDate.atTime(0, 0,0,1), "d");//3월 15일 0시로 집계되어야 함.

            insertSupportCase(memberId,targetDate.atTime(0,59,59,59),"e");//3월 14일 0시로 집계되어야 함.
            insertSupportCase(memberId,targetDate.atTime(1,0,0,1),"f");//3월 14일 1시로 집계되어야 함.

            //when
            List<CounselTrafficDailyRawData> result = repository.fetchCounselTrafficByHour(targetDate);

            //then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly(
                    new CounselTrafficDailyRawData(0, 3),
                    new CounselTrafficDailyRawData(1, 1),
                    new CounselTrafficDailyRawData(23, 1)
            );
        }


        @Test
        @DisplayName("일 단위 조회 시 같은 날짜의 일별 건수를 집계한다.")
        void get_counsel_traffic_by_daily() {
            //given
            Long memberId = createDummyMember();
            LocalDate startDate = LocalDate.of(2026, 3, 1);
            LocalDate endDate = LocalDate.of(2026,3,31);

            YearMonth targetMonth_1 = YearMonth.of(2026,3);
            YearMonth targetMonth_2 = YearMonth.of(2026,12);
            YearMonth targetMonth_3 = YearMonth.of(2027,1);

            LocalDate endDateOfYear = LocalDate.of(2026,12,31);
            LocalDate startDateOfYear = LocalDate.of(2027,1,1);

            /**
             * 일자별 경계
             */
            insertSupportCase(memberId, startDate.atTime(0, 0,0,0), "a");//3월 1일 집계되어야 함.
            insertSupportCase(memberId, startDate.atTime(0, 0,0,1), "b");//3월 1일 로 집계되어야 함.
            insertSupportCase(memberId, startDate.atTime(23, 59,59,59), "c");//3월 1일로 집계되어야 함.

            insertSupportCase(memberId, endDate.atTime(23, 59,59,59), "d");//3월 31일로 집계되어야 함.

            /**
             * 월 경계
             */
            insertSupportCase(memberId, endDateOfYear.atTime(23, 59,59,59), "e");//12월 31일로 집계되어야 함.
            insertSupportCase(memberId, startDateOfYear.atTime(0, 0,0,0), "f");//1월 1일로 집계되어야 함.

            //when
            List<CounselTrafficMonthlyRawData> result_1 = repository.fetchCounselTrafficByDay(targetMonth_1);
            List<CounselTrafficMonthlyRawData> result_2 = repository.fetchCounselTrafficByDay(targetMonth_2);
            List<CounselTrafficMonthlyRawData> result_3 = repository.fetchCounselTrafficByDay(targetMonth_3);

            //then
            assertThat(result_1).hasSize(2);
            assertThat(result_1).containsExactly(
                    new CounselTrafficMonthlyRawData(1, 3),
                    new CounselTrafficMonthlyRawData(31, 1)
            );

            assertThat(result_2).hasSize(1);
            assertThat(result_2).containsExactly(
                    new CounselTrafficMonthlyRawData(31, 1)
            );

            assertThat(result_3).hasSize(1);
            assertThat(result_3).containsExactly(
                    new CounselTrafficMonthlyRawData(1, 1)
            );
        }
    }


    /**
     * 더미 멤버 생성
     */
    private Long createDummyMember() {
        String suffix = String.valueOf(System.nanoTime());
        return dsl.insertInto(MEMBER)
                .set(MEMBER.PROVIDER_ID, "provider-" + suffix)
                .set(MEMBER.EMAIL, TEST_EMAIL_PREFIX + suffix + "@example.com")
                .set(MEMBER.NAME, "테스트회원-" + suffix)
                .set(MEMBER.STATUS, MemberStatusType.ACTIVE)
                .set(MEMBER.TYPE, MemberSignupType.GOOGLE)
                .set(MEMBER.ROLE, MemberRoleType.CUSTOMER)
                .set(MEMBER.JOIN_DATE, LocalDate.of(2099, 1, 1))
                .returningResult(MEMBER.MEMBER_ID)
                .fetchSingle()
                .into(Long.class);
    }

    /**
     * 더미 카테고리 생성
     */
    private void ensureCategory() {
        dsl.insertInto(CATEGORY_GROUP)
                .set(CATEGORY_GROUP.CATEGORY_GROUP_CODE, TEST_CATEGORY_GROUP_CODE)
                .set(CATEGORY_GROUP.CATEGORY_NAME, "테스트 대분류")
                .onConflict(CATEGORY_GROUP.CATEGORY_GROUP_CODE)
                .doNothing()
                .execute();

        dsl.insertInto(CATEGORY)
                .set(CATEGORY.CATEGORY_CODE, TEST_CATEGORY_CODE)
                .set(CATEGORY.CATEGORY_GROUP_CODE, TEST_CATEGORY_GROUP_CODE)
                .set(CATEGORY.CATEGORY_NAME, "테스트 소분류")
                .onConflict(CATEGORY.CATEGORY_CODE)
                .doNothing()
                .execute();
    }

    /**
     * 더미 상담 데이터 생성
     */
    private void insertSupportCase(Long memberId, LocalDateTime createdAt, String suffix) {
        dsl.insertInto(SUPPORT_CASE)
                .set(SUPPORT_CASE.MEMBER_ID, memberId)
                .set(SUPPORT_CASE.CATEGORY_CODE, TEST_CATEGORY_CODE)
                .set(SUPPORT_CASE.STATUS, SupportStatus.OPEN)
                .set(SUPPORT_CASE.TITLE, TEST_TITLE_PREFIX + suffix)
                .set(SUPPORT_CASE.QUESTION_TEXT, "dummy-question")
                .set(SUPPORT_CASE.CREATED_AT, createdAt)
                .set(SUPPORT_CASE.UPDATED_AT, createdAt)
                .execute();
    }
}
