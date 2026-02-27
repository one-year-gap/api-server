package site.holliverse.admin.query.dao;

import org.jooq.DSLContext;
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

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;


//로컬에서 테스트 성공후 pr에 사진 첨부
@Disabled("CI 환경에서는 PostgreSQL DB 연결이 불가능하므로 임시 비활성화")
@ActiveProfiles("admin")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AdminMembershipStatDao.class, AdminMembershipStatDaoTest.TestConfig.class})
class AdminMembershipStatDaoTest {

    @Autowired
    private AdminMembershipStatDao adminMembershipStatDao;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DSLContext dslContext(DataSource dataSource) {
            return new DefaultDSLContext(dataSource, SQLDialect.POSTGRES);
        }
    }

    @Test
    @DisplayName("DAO 쿼리 결과가 기대 카운트와 일치한다")
    void getMembershipStats_returnsExpectedCounts() {
        AdminMembershipStatRawData raw = adminMembershipStatDao.getMembershipStats();

        assertThat(raw).isNotNull();
        assertThat(raw.totalCount()).isEqualTo(1000L);
        assertThat(raw.vvipCount()).isEqualTo(91L);
        assertThat(raw.vipCount()).isEqualTo(207L);
        assertThat(raw.goldCount()).isEqualTo(702L);
    }
}
