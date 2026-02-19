package site.holliverse.admin.query.dao;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import site.holliverse.admin.web.dto.member.AdminMemberListRequestDto;
import site.holliverse.shared.util.EncryptionTool;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static site.holliverse.admin.query.jooq.tables.Member.MEMBER;
import static site.holliverse.admin.query.jooq.tables.Address.ADDRESS;
import site.holliverse.admin.query.jooq.enums.*;

@ActiveProfiles("admin")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AdminMemberDao.class, AdminMemberDaoTest.TestConfig.class})
class AdminMemberDaoTest {

    @Autowired
    private AdminMemberDao adminMemberDao;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private EncryptionTool encryptionTool;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public EncryptionTool encryptionTool() {
            String testKey = "12345678901234567890123456789012";
            return new EncryptionTool(testKey, "AES", "AES/ECB/PKCS5Padding");
        }

        @Bean
        public DSLContext dslContext(DataSource dataSource) {
            return new DefaultDSLContext(dataSource, SQLDialect.POSTGRES);
        }
    }

    private Long createDummyAddress() {
        // [중복 방지] 주소 뒤에 나노초 추가
        String uniqueStreet = "판교역로 " + System.nanoTime();

        return dsl.insertInto(ADDRESS)
                .set(ADDRESS.PROVINCE, "경기도")
                .set(ADDRESS.CITY, "성남시")
                .set(ADDRESS.STREET_ADDRESS, uniqueStreet)
                .set(ADDRESS.POSTAL_CODE, "13529")
                .returningResult(ADDRESS.ADDRESS_ID)
                .fetchSingle()
                .into(Long.class);
    }

    // 랜덤 전화번호 생성 헬퍼 (010 + 8자리 난수)
    private String generateRandomPhone() {
        Random random = new Random();
        int number = random.nextInt(90000000) + 10000000; // 10000000 ~ 99999999
        return "010" + number;
    }

    @Test
    @DisplayName("데이터가 15개일 때, 페이징(size=10)을 요청하면 정확히 10개만 반환되어야 한다.")
    void findAll_paging_test() {
        // 1. 주소 생성
        Long addressId = createDummyAddress();

        long now = System.currentTimeMillis();

        // 2. 데이터 15개 삽입
        for (int i = 1; i <= 15; i++) {
            // [중복 방지] 전화번호 랜덤 생성
            String uniquePhone = generateRandomPhone();
            // [중복 방지] 이메일 유니크 생성
            String uniqueEmail = "user" + i + "_" + now + "@test.com";

            dsl.insertInto(MEMBER)
                    .set(MEMBER.ADDRESS_ID, addressId)
                    .set(MEMBER.EMAIL, uniqueEmail)
                    .set(MEMBER.PASSWORD, "dummy1234")
                    .set(MEMBER.NAME, encryptionTool.encrypt("유저" + i))
                    .set(MEMBER.PHONE, encryptionTool.encrypt(uniquePhone))
                    .set(MEMBER.BIRTH_DATE, LocalDate.of(2000, 1, 1))
                    .set(MEMBER.GENDER, "M")
                    .set(MEMBER.STATUS, MemberStatusType.ACTIVE)
                    .set(MEMBER.TYPE, MemberSignupType.FORM)
                    .set(MEMBER.ROLE, MemberRoleType.CUSTOMER)
                    .set(MEMBER.MEMBERSHIP, MemberMembershipType.VIP)
                    .set(MEMBER.JOIN_DATE, LocalDate.now())
                    .execute();
        }

        // when
        AdminMemberListRequestDto req = new AdminMemberListRequestDto(
                1, 10, null, null, null, null, null, null, null
        );
        List<MemberRawData> result = adminMemberDao.findAll(req);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(10);
    }

    @Test
    @DisplayName("DB에 '김영현'이 있어도 '박영현'을 검색하면 결과가 없어야 한다.")
    void findAll_no_result_test() {
        // 1. 주소 생성
        Long addressId = createDummyAddress();

        String uniqueEmail = "yh.kim." + System.nanoTime() + "@test.com";
        // [중복 방지] 전화번호 랜덤 생성
        String uniquePhone = generateRandomPhone();

        // 2. '김영현' 저장
        dsl.insertInto(MEMBER)
                .set(MEMBER.ADDRESS_ID, addressId)
                .set(MEMBER.EMAIL, uniqueEmail)
                .set(MEMBER.PASSWORD, "password123")
                .set(MEMBER.NAME, encryptionTool.encrypt("김영현"))
                .set(MEMBER.PHONE, encryptionTool.encrypt(uniquePhone))
                .set(MEMBER.BIRTH_DATE, LocalDate.of(1995, 5, 5))
                .set(MEMBER.GENDER, "M")
                .set(MEMBER.STATUS, MemberStatusType.ACTIVE)
                .set(MEMBER.TYPE, MemberSignupType.FORM)
                .set(MEMBER.ROLE, MemberRoleType.CUSTOMER)
                .set(MEMBER.MEMBERSHIP, MemberMembershipType.VIP)
                .execute();

        // when
        AdminMemberListRequestDto req = new AdminMemberListRequestDto(
                1, 10, "박영현", null, null, null, null, null, null
        );
        List<MemberRawData> result = adminMemberDao.findAll(req);

        // then
        assertThat(result).isEmpty();
    }
}