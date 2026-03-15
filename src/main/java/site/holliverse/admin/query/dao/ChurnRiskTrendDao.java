package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.List;

import static site.holliverse.admin.query.jooq.Tables.CHURN_SCORE_SNAPSHOT;

/**
 * 이탈 위험군 증감 추이용 조회.
 * 이탈 위험도 테이블(churn_score_snapshot)에서 risk_level = 'HIGH'인 회원만 일별 집계.
 */
@Profile("admin")
@RequiredArgsConstructor
public class ChurnRiskTrendDao {

    private final DSLContext dsl;

    /**
     * 기간 내 일별 이탈 위험군(HIGH) 인원 수 조회.
     *
     * @param from 조회 시작일 (base_date 포함)
     * @param to   조회 종료일 (base_date 포함)
     * @return base_date 오름차순 (baseDate, count)
     */
    public List<DailyRiskCount> findDailyHighRiskCounts(LocalDate from, LocalDate to) {
        return dsl
                .select(
                        CHURN_SCORE_SNAPSHOT.BASE_DATE.as("baseDate"),
                        DSL.count().as("count")
                )
                .from(CHURN_SCORE_SNAPSHOT)
                .where(CHURN_SCORE_SNAPSHOT.BASE_DATE.between(from, to))
                .and(CHURN_SCORE_SNAPSHOT.RISK_LEVEL.eq("HIGH"))
                .groupBy(CHURN_SCORE_SNAPSHOT.BASE_DATE)
                .orderBy(CHURN_SCORE_SNAPSHOT.BASE_DATE)
                .fetchInto(DailyRiskCount.class);
    }
}
