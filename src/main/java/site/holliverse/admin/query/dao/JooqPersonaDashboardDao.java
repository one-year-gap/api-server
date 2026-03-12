package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import site.holliverse.admin.query.jooq.enums.ProductTypeEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

import static site.holliverse.admin.query.jooq.Tables.INDEX_PERSONA_SNAPSHOT;
import static site.holliverse.admin.query.jooq.Tables.PERSONA_TYPE;
import static site.holliverse.admin.query.jooq.Tables.PRODUCT;
import static site.holliverse.admin.query.jooq.Tables.SUBSCRIPTION;

/**
 * 페르소나 대시보드 통계 조회
 */
@Profile("admin")
@Repository
@RequiredArgsConstructor
public class JooqPersonaDashboardDao implements PersonaDashboardDao {

    private final DSLContext dsl;

    /**
     * 특정 날짜의 페르소나별 유저 수 및 Top 3 요금제 조회
     */
    @Override
    public List<PersonaDistributionData> findDistributionAndTopPlansByDate(LocalDate targetDate) {

        // ==========================================
        // 1. 페르소나별 전체 유저 수 집계
        // ==========================================
        Field<String> personaName = PERSONA_TYPE.CHARACTER_NAME.as("personaName");
        Field<Integer> userCount = DSL.count(INDEX_PERSONA_SNAPSHOT.MEMBER_ID).as("userCount");

        // Map<페르소나명, 유저수> 형태로 조회
        Map<String, Long> userCountMap = dsl
                .select(personaName, userCount)
                .from(INDEX_PERSONA_SNAPSHOT)
                .join(PERSONA_TYPE).on(INDEX_PERSONA_SNAPSHOT.PERSONA_TYPE_ID.eq(PERSONA_TYPE.PERSONA_TYPE_ID))
                .where(INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE.eq(targetDate))
                .groupBy(PERSONA_TYPE.CHARACTER_NAME)
                .fetchMap(personaName, r -> toLong(r.get(userCount)));

        // ==========================================
        // 2. 페르소나별 Top 3 모바일 요금제 집계 (Window Function 사용)
        // ==========================================
        Field<String> planName = PRODUCT.NAME.as("planName");

        // 윈도우 함수: 페르소나 그룹 내에서 구독자 수 기준으로 내림차순 랭킹 부여
        Field<Integer> rankNo = DSL.rowNumber().over(
                DSL.partitionBy(PERSONA_TYPE.CHARACTER_NAME)
                        .orderBy(DSL.count(SUBSCRIPTION.MEMBER_ID).desc(), PRODUCT.NAME.asc())
        ).as("rankNo");

        // 서브쿼리: 요금제별 랭킹을 매긴 테이블 생성
        Table<?> rankedPlans = dsl
                .select(personaName, planName, rankNo)
                .from(INDEX_PERSONA_SNAPSHOT)
                .join(PERSONA_TYPE).on(INDEX_PERSONA_SNAPSHOT.PERSONA_TYPE_ID.eq(PERSONA_TYPE.PERSONA_TYPE_ID))
                .join(SUBSCRIPTION).on(INDEX_PERSONA_SNAPSHOT.MEMBER_ID.eq(SUBSCRIPTION.MEMBER_ID))
                .join(PRODUCT).on(SUBSCRIPTION.PRODUCT_ID.eq(PRODUCT.PRODUCT_ID))
                .where(INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE.eq(targetDate))
                .and(SUBSCRIPTION.STATUS.isTrue()) // 활성 구독만
                .and(PRODUCT.PRODUCT_TYPE.eq(ProductTypeEnum.MOBILE_PLAN)) // 모바일 요금제만
                .groupBy(PERSONA_TYPE.CHARACTER_NAME, PRODUCT.NAME)
                .asTable("rankedPlans");

        // 서브쿼리에서 랭킹 3위 이하인 데이터만 추출하여 Map<페르소나명, List<요금제명>> 형태로 묶기
        Map<String, List<String>> topPlansMap = dsl
                .select(
                        rankedPlans.field(personaName),
                        rankedPlans.field(planName)
                )
                .from(rankedPlans)
                .where(Objects.requireNonNull(rankedPlans.field(rankNo)).le(3))
                .orderBy(
                        Objects.requireNonNull(rankedPlans.field(personaName)).asc(),
                        Objects.requireNonNull(rankedPlans.field(rankNo)).asc()
                )
                .fetchGroups(rankedPlans.field(personaName), rankedPlans.field(planName));

        // ==========================================
        // 3. 자바 스트림으로 두 데이터 조합하여 DTO 반환
        // ==========================================
        return userCountMap.entrySet().stream()
                .map(entry -> new PersonaDistributionData(
                        entry.getKey(),
                        entry.getValue(),
                        topPlansMap.getOrDefault(entry.getKey(), List.of()) // 요금제가 없으면 빈 리스트
                ))
                // 유저 수 기준 내림차순 정렬
                .sorted((d1, d2) -> Long.compare(d2.userCount(), d1.userCount()))
                .collect(Collectors.toList());
    }

    /**
     * 월별 페르소나 트렌드 조회
     * 매월 1일~말일 모든 데이터를 긁어모으면 집계가 부풀려지므로,
     * "각 월별로 데이터가 존재하는 '가장 마지막 날짜(MAX)'의 스냅샷만" 필터링하여 카운트
     */
    @Override
    public List<PersonaMonthlyTrendData> findMonthlyTrendByPeriod(LocalDate startDate, LocalDate endDate) {

        // YYYY-MM 형태로 날짜를 자르는 DB 함수 (PostgreSQL 기준 TO_CHAR)
        Field<String> yyyyMm = DSL.function("TO_CHAR", String.class, INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE, DSL.inline("YYYY-MM")).as("yyyyMm");

        // 서브쿼리: 월별로 스냅샷이 존재하는 가장 최근(마지막) 날짜 찾기
        Field<LocalDate> maxDate = DSL.max(INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE).as("maxDate");
        Table<?> endOfMonthSnapshots = dsl
                .select(maxDate)
                .from(INDEX_PERSONA_SNAPSHOT)
                .where(INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE.between(startDate, endDate))
                .groupBy(yyyyMm)
                .asTable("endOfMonthSnapshots");

        Field<Integer> userCount = DSL.count(INDEX_PERSONA_SNAPSHOT.MEMBER_ID).as("userCount");

        // 찾은 '월별 마지막 날짜'와 일치하는 스냅샷 데이터만 조인해서 진짜 월말 유저 수를 집계
        return dsl.select(
                        yyyyMm,
                        PERSONA_TYPE.CHARACTER_NAME,
                        userCount
                )
                .from(INDEX_PERSONA_SNAPSHOT)
                .join(PERSONA_TYPE).on(INDEX_PERSONA_SNAPSHOT.PERSONA_TYPE_ID.eq(PERSONA_TYPE.PERSONA_TYPE_ID))
                // 월말 날짜 필터링 조인
                .join(endOfMonthSnapshots).on(INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE.eq(endOfMonthSnapshots.field(maxDate)))
                .groupBy(yyyyMm, PERSONA_TYPE.CHARACTER_NAME)
                .orderBy(yyyyMm.asc(), userCount.desc())
                .fetch(r -> new PersonaMonthlyTrendData(
                        r.get(yyyyMm),
                        r.get(PERSONA_TYPE.CHARACTER_NAME),
                        toLong(r.get(userCount))
                ));
    }

    // null safe 처리를 위한 유틸리티 메서드
    private long toLong(Integer value) {
        return value == null ? 0L : value.longValue();
    }

    @Override
    public LocalDate findLatestSnapshotDate() {
        return dsl.select(DSL.max(INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE))
                .from(INDEX_PERSONA_SNAPSHOT)
                .fetchOneInto(LocalDate.class);
    }
}