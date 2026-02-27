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
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("로컬 PostgreSQL 실데이터 기준 검증 테스트")
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
            // 운영과 동일한 PostgreSQL 문법/함수 동작을 강제하기 위해 SQLDialect.POSTGRES 사용
            return new DefaultDSLContext(dataSource, SQLDialect.POSTGRES);
        }
    }

    @Test
    @DisplayName("현재 더미 데이터 기준 멤버십 통계가 기대값과 일치한다")
    void getMembershipStats_matchesCurrentSeedData() {
        // given/when:
        // - DAO를 통해 현재 DB 상태의 멤버십 원시 카운트(total/vvip/vip/gold)를 조회한다.
        // - 이 테스트는 데이터를 새로 넣지 않고, 기존 더미/실데이터를 그대로 사용한다.
        AdminMembershipStatRawData raw = adminMembershipStatDao.getMembershipStats();

        // then(기본 방어):
        // - 조회 결과 자체가 null이 아니어야 한다.
        // - totalCount가 null/0이면 이후 비율 계산의 분모가 0이 되므로, 최소 1 이상인지 확인한다.
        assertThat(raw).isNotNull();
        assertThat(raw.totalCount()).isNotNull();
        assertThat(raw.totalCount()).isGreaterThan(0L);

        // null-safe 변환:
        // jOOQ 매핑 이슈/alias mismatch가 있으면 특정 필드가 null로 들어올 수 있어
        // 계산 단계에서 NPE가 나지 않도록 0으로 치환한다.
        long total = raw.totalCount();
        long vvip = raw.vvipCount() == null ? 0L : raw.vvipCount();
        long vip = raw.vipCount() == null ? 0L : raw.vipCount();
        long gold = raw.goldCount() == null ? 0L : raw.goldCount();

        // API 응답 규칙과 동일하게 total을 k 단위(소수 1자리)로 변환한다.
        // 예) 1000 -> 1.0, 14235 -> 14.2
        BigDecimal totalInK = BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP);

        // 비율 계산 규칙:
        // - 100.0%를 0.1% 단위 tick 1000개로 환산해 정수 연산한다.
        // - 먼저 floor로 기본 배분을 만들고, 남은 tick을 잔여량이 큰 순서대로 재분배한다.
        // - 이렇게 해야 최종 합계가 정확히 100.0이 된다.
        long totalTicks = 1000L;
        long vvipTickFloor = (vvip * totalTicks) / total;
        long vipTickFloor = (vip * totalTicks) / total;
        long goldTickFloor = (gold * totalTicks) / total;

        long used = vvipTickFloor + vipTickFloor + goldTickFloor;
        long remain = totalTicks - used;

        // floor 계산으로 버려진 소수 부분(잔여량)
        long vvipRem = (vvip * totalTicks) % total;
        long vipRem = (vip * totalTicks) % total;
        long goldRem = (gold * totalTicks) % total;

        // 잔여량이 큰 항목부터 1tick씩 추가 배분
        for (long i = 0; i < remain; i++) {
            if (vvipRem >= vipRem && vvipRem >= goldRem) {
                vvipTickFloor++;
                vvipRem = -1;
            } else if (vipRem >= vvipRem && vipRem >= goldRem) {
                vipTickFloor++;
                vipRem = -1;
            } else {
                goldTickFloor++;
                goldRem = -1;
            }
        }

        // tick -> 소수 1자리 퍼센트로 역변환 (예: 334 -> 33.4)
        BigDecimal vvipRate = BigDecimal.valueOf(vvipTickFloor)
                .divide(BigDecimal.TEN, 1, RoundingMode.UNNECESSARY);
        BigDecimal vipRate = BigDecimal.valueOf(vipTickFloor)
                .divide(BigDecimal.TEN, 1, RoundingMode.UNNECESSARY);
        BigDecimal goldRate = BigDecimal.valueOf(goldTickFloor)
                .divide(BigDecimal.TEN, 1, RoundingMode.UNNECESSARY);

        // 현재 로컬 더미데이터 기준 기대값 스냅샷 검증
        assertThat(totalInK).isEqualByComparingTo("1.0");
        assertThat(vvipRate).isEqualByComparingTo("9.1");
        assertThat(vipRate).isEqualByComparingTo("20.7");
        assertThat(goldRate).isEqualByComparingTo("70.2");
    }
}
