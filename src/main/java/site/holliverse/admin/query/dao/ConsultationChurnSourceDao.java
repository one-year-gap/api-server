package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import static site.holliverse.admin.query.jooq.Tables.SUPPORT_CASE;

@Repository
@Profile("admin")
@RequiredArgsConstructor
public class ConsultationChurnSourceDao {

    private final DSLContext dsl;

    public double findAverageSatisfactionScore(Long memberId) {
        // 만족도 평균 조회
        Double averageScore = dsl.select(DSL.avg(SUPPORT_CASE.SATISFACTION_SCORE).cast(Double.class))
                .from(SUPPORT_CASE)
                .where(SUPPORT_CASE.MEMBER_ID.eq(memberId))
                .and(SUPPORT_CASE.SATISFACTION_SCORE.isNotNull())
                .fetchOneInto(Double.class);

        // 기본 점수
        return averageScore != null ? averageScore : 0.0d;
    }
}
