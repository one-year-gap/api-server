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

        // 1. 날짜 포맷팅 함수 정의 (PostgreSQL TO_CHAR 사용)
        Field<String> toCharFunc = DSL.function("TO_CHAR", String.class, INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE, DSL.inline("YYYY-MM"));

        // 서브쿼리 SELECT 절에서 사용할 별명(Alias)들
        Field<String> yyyyMmAlias = toCharFunc.as("yyyyMm");
        Field<LocalDate> maxDateAlias = DSL.max(INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE).as("maxDate");

        // [서브쿼리] 각 월별(YYYY-MM) 가장 마지막 스냅샷 날짜 구하기
        // 결과 예시: (2025-10, 2025-10-31), (2025-11, 2025-11-30)
        Table<?> endOfMonthSnapshots = dsl
                .select(yyyyMmAlias, maxDateAlias)
                .from(INDEX_PERSONA_SNAPSHOT)
                .where(INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE.between(startDate, endDate))
                // 별명("yyyyMm")으로 묶으면 PostgreSQL에서 에러가 날 수 있으므로, 원본 함수(toCharFunc)로 안전하게 묶음
                .groupBy(toCharFunc)
                .asTable("endOfMonthSnapshots");

        // 2. 유저 카운트 필드
        Field<Integer> userCount = DSL.count(INDEX_PERSONA_SNAPSHOT.MEMBER_ID).as("userCount");

        // IDE 경고(NPE) 방어 및 안전한 필드 참조
        Field<String> safeYyyyMm = Objects.requireNonNull(endOfMonthSnapshots.field("yyyyMm", String.class));
        Field<LocalDate> safeMaxDate = Objects.requireNonNull(endOfMonthSnapshots.field("maxDate", LocalDate.class));

        // [메인 쿼리] 서브쿼리에서 찾은 '월말 날짜'와 일치하는 스냅샷만 조인하여 최종 집계
        return dsl.select(
                        safeYyyyMm, // 메인 쿼리에서 TO_CHAR를 또 실행하지 않고, 서브쿼리의 결과 컬럼을 재사용
                        PERSONA_TYPE.CHARACTER_NAME,
                        userCount
                )
                .from(INDEX_PERSONA_SNAPSHOT)
                .join(PERSONA_TYPE).on(INDEX_PERSONA_SNAPSHOT.PERSONA_TYPE_ID.eq(PERSONA_TYPE.PERSONA_TYPE_ID))
                // 스냅샷 날짜가 '해당 월의 마지막 스냅샷 날짜'와 같은 데이터만 조인
                .join(endOfMonthSnapshots).on(INDEX_PERSONA_SNAPSHOT.SNAPSHOT_DATE.eq(safeMaxDate))
                // 년-월, 페르소나 이름 기준으로 그룹핑
                .groupBy(safeYyyyMm, PERSONA_TYPE.CHARACTER_NAME)
                // 시간순 정렬 후, 같은 달 안에서는 유저 수가 많은 순서대로 정렬
                .orderBy(safeYyyyMm.asc(), userCount.desc())
                .fetch(r -> new PersonaMonthlyTrendData(
                        // 최종 DTO 변환 시에도 가장 안전한 safeYyyyMm 필드를 사용하여 값을 꺼냄
                        r.get(safeYyyyMm),
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