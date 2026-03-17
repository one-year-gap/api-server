package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Row6;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.application.usecase.dto.AnalysisResponseCommand;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.excluded;
import static org.jooq.impl.DSL.row;
import static site.holliverse.admin.query.jooq.Tables.BUSINESS_KEYWORD_MAPPING_RESULT;
import static site.holliverse.admin.query.jooq.Tables.CONSULTATION_ANALYSIS;

@Repository
@Profile("admin")
@RequiredArgsConstructor
public class ConsultationAnalysisDao {
    private final DSLContext dsl;

    @Transactional
    public void save(AnalysisResponseCommand command) {
        // 분석 이력 저장
        Long analysisId = upsertConsultationAnalysis(command);

        // 키워드 매핑 저장
        replaceKeywordMappings(analysisId, command.keywordCounts());
    }

    private Long upsertConsultationAnalysis(AnalysisResponseCommand command) {
        // 저장 시각
        LocalDateTime now = LocalDateTime.now();

        // 분석 이력 업서트
        return dsl.insertInto(
                        CONSULTATION_ANALYSIS,
                        CONSULTATION_ANALYSIS.CASE_ID,
                        CONSULTATION_ANALYSIS.JOB_INSTANCE_ID,
                        CONSULTATION_ANALYSIS.ANALYZER_VERSION,
                        CONSULTATION_ANALYSIS.CREATED_AT,
                        CONSULTATION_ANALYSIS.UPDATED_AT
                )
                .values(
                        command.caseId(),
                        command.analysisId(),
                        command.analyzerVersion(),
                        now,
                        now
                )
                .onConflict(CONSULTATION_ANALYSIS.CASE_ID, CONSULTATION_ANALYSIS.ANALYZER_VERSION)
                .doUpdate()
                .set(CONSULTATION_ANALYSIS.JOB_INSTANCE_ID, excluded(CONSULTATION_ANALYSIS.JOB_INSTANCE_ID))
                .set(CONSULTATION_ANALYSIS.UPDATED_AT, now)
                .returning(CONSULTATION_ANALYSIS.ANALYSIS_ID)
                .fetchOne(CONSULTATION_ANALYSIS.ANALYSIS_ID);
    }

    private void replaceKeywordMappings(
            Long analysisId,
            List<AnalysisResponseCommand.KeywordCountCommand> keywordCounts
    ) {
        // 기존 매핑 삭제
        dsl.deleteFrom(BUSINESS_KEYWORD_MAPPING_RESULT)
                .where(BUSINESS_KEYWORD_MAPPING_RESULT.ANALYSIS_ID.eq(analysisId))
                .execute();

        // 빈 목록 종료
        if (keywordCounts == null || keywordCounts.isEmpty()) {
            return;
        }

        // 키워드 집계
        Map<Long, Integer> aggregated = new HashMap<>();
        for (AnalysisResponseCommand.KeywordCountCommand item : keywordCounts) {
            Long businessKeywordId = item.businessKeywordId();
            if (businessKeywordId == null) {
                continue;
            }

            int countValue = item.count() != null ? item.count() : 0;
            aggregated.merge(businessKeywordId, countValue, Integer::sum);
        }

        // 빈 집계 종료
        if (aggregated.isEmpty()) {
            return;
        }

        // 저장 시각
        LocalDateTime now = LocalDateTime.now();
        List<Row6<Long, Long, Integer, BigDecimal, LocalDateTime, LocalDateTime>> rows =
                new ArrayList<>(aggregated.size());

        // 삽입 값 구성
        for (Map.Entry<Long, Integer> entry : aggregated.entrySet()) {
            rows.add(row(
                    analysisId,
                    entry.getKey(),
                    entry.getValue(),
                    BigDecimal.ZERO,
                    now,
                    now
            ));
        }

        // 매핑 일괄 저장
        dsl.insertInto(
                        BUSINESS_KEYWORD_MAPPING_RESULT,
                        BUSINESS_KEYWORD_MAPPING_RESULT.ANALYSIS_ID,
                        BUSINESS_KEYWORD_MAPPING_RESULT.BUSINESS_KEYWORD_ID,
                        BUSINESS_KEYWORD_MAPPING_RESULT.COUNT,
                        BUSINESS_KEYWORD_MAPPING_RESULT.CHANGE_RATE,
                        BUSINESS_KEYWORD_MAPPING_RESULT.CREATED_AT,
                        BUSINESS_KEYWORD_MAPPING_RESULT.UPDATED_AT
                )
                .valuesOfRows(rows)
                .execute();
    }
}
