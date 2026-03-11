package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import static site.holliverse.admin.query.jooq.Tables.MEMBER;
import static site.holliverse.admin.query.jooq.Tables.SUBSCRIPTION;
import site.holliverse.admin.query.jooq.enums.MemberStatusType;

@Repository
@RequiredArgsConstructor
public class MemberStatisticsDao {

    private final DSLContext dsl;

    /**
     * 특정 날짜(startDate) 이후의 월별 가입자 수를 조회
     * * @param startDate 조회 시작 기준일 (예: 9개월 전 1일)
     * @return Map<"YYYY-MM", 가입자 수> (예: {"2026-03": 150, "2026-02": 120})
     */
    public Map<String, Long> getJoinedCountByMonth(LocalDate startDate) {

        // 1. 월별 그룹핑을 위한 커스텀 필드 생성 (PostgreSQL의 to_char 함수 사용)
        // {0} 자리에 MEMBER.JOIN_DATE 필드가 들어가서 "to_char(join_date, 'YYYY-MM')" 형태의 SQL이 만들어짐
        // 반환 타입이 String이므로 Field<String>으로 명시
        Field<String> monthField = DSL.field("to_char({0}, 'YYYY-MM')", String.class, MEMBER.JOIN_DATE);

        // 2. 가입자 수를 세는(COUNT) 필드 생성
        Field<Integer> countField = DSL.count(MEMBER.MEMBER_ID);

        return dsl.select(monthField, countField)
                .from(MEMBER)
                // 문자열 비교가 아닌 jOOQ 필드의 메서드(.ge)를 사용하여 크거나 같은지(>=) 안전하게 비교
                .where(MEMBER.JOIN_DATE.ge(startDate))
                // 가입 진행 중(PROCESSING)인 허수 데이터는 통계에서 제외
                .and(MEMBER.STATUS.ne(MemberStatusType.PROCESSING))
                .groupBy(monthField)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        // Map의 Key: "YYYY-MM" 형태의 문자열
                        record -> record.get(monthField),

                        // Map의 Value: 해당 월의 가입자 수 (Long 타입으로 변환)
                        record -> {
                            Integer count = record.get(countField);
                            return count != null ? count.longValue() : 0L;
                        }
                ));
    }

    /**
     * 특정 날짜(startDate) 이후의 월별 탈퇴자(구독 해지) 수를 조회
     * (동일 회원의 다중 해지 방지를 위해 countDistinct 적용)
     * * @param startDate 조회 시작 기준일
     * @return Map<"YYYY-MM", 탈퇴자 수>
     */
    public Map<String, Long> getLeftCountByMonth(LocalDate startDate) {

        // 1. 월별 그룹핑을 위한 커스텀 필드 생성 (해지일인 SUBSCRIPTION.END_DATE 기준)
        Field<String> monthField = DSL.field("to_char({0}, 'YYYY-MM')", String.class, SUBSCRIPTION.END_DATE);

        // 2. 중복을 제거한 멤버 수 카운트 필드 (COUNT(DISTINCT member_id))
        // 한 회원이 여러 상품을 해지해도 1명으로 치기 위해 DSL.countDistinct()를 사용
        Field<Integer> countField = DSL.countDistinct(SUBSCRIPTION.MEMBER_ID);

        return dsl.select(monthField, countField)
                .from(SUBSCRIPTION)
                // status가 false(해지됨)인 데이터만 필터링
                .where(SUBSCRIPTION.STATUS.eq(false))
                // 파라미터로 받은 startDate는 날짜(LocalDate)이므로 .atStartOfDay()를 붙여 자정(00:00:00) 시간으로 맞춰서 비교
                .and(SUBSCRIPTION.END_DATE.ge(startDate.atStartOfDay()))
                .groupBy(monthField)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record -> record.get(monthField),
                        record -> {
                            Integer count = record.get(countField);
                            return count != null ? count.longValue() : 0L;
                        }
                ));
    }
}