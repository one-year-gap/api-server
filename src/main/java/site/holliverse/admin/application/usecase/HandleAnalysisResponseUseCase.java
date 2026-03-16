//package site.holliverse.admin.application.usecase;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jooq.DSLContext;
//import org.jooq.Record5;
//import org.jooq.Row6;
//import org.jooq.Row5;
//import org.jooq.Result;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import site.holliverse.admin.application.usecase.dto.AnalysisResponseCommand;
//import site.holliverse.admin.query.jooq.enums.AnalysisStatus;
//import site.holliverse.admin.query.jooq.enums.DispatchOutboxType;
//import site.holliverse.admin.query.jooq.enums.DispatchStatus;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.jooq.impl.DSL.count;
//import static org.jooq.impl.DSL.currentLocalDateTime;
//import static org.jooq.impl.DSL.excluded;
//import static org.jooq.impl.DSL.row;
//import static site.holliverse.admin.query.jooq.Tables.ANALYSIS_DISPATCH_OUTBOX;
//import static site.holliverse.admin.query.jooq.Tables.BUSINESS_KEYWORD_MAPPING_RESULT;
//import static site.holliverse.admin.query.jooq.Tables.CONSULTATION_ANALYSIS;
//
//@Service
//@Profile("admin")
//@Slf4j
//@RequiredArgsConstructor
//public class HandleAnalysisResponseUseCase {
//    @Value("${app.kafka.analysis-response-schema:analysis.response.v1}")
//    private String supportedSchema;
//
//    private final DSLContext dsl;
//    private final AnalysisDispatchOutboxRetryService retryService;
//
//    /**
//     * Kafka payload를 검증 -> outbox(request_id) 기준으로 멱등 처리
//     */
//    @Transactional
//    public void execute(AnalysisResponseCommand payload) {
//        validate(payload);
//        try {
//            Record5<String, String, Long, DispatchOutboxType, DispatchStatus> outbox = dsl
//                    .select(
//                            ANALYSIS_DISPATCH_OUTBOX.REQUEST_ID,
//                            ANALYSIS_DISPATCH_OUTBOX.CHUNK_ID,
//                            ANALYSIS_DISPATCH_OUTBOX.JOB_INSTANCE_ID,
//                            ANALYSIS_DISPATCH_OUTBOX.TYPE,
//                            ANALYSIS_DISPATCH_OUTBOX.DISPATCH_STATUS
//                    )
//                    .from(ANALYSIS_DISPATCH_OUTBOX)
//                    .where(ANALYSIS_DISPATCH_OUTBOX.REQUEST_ID.eq(payload.dispatchRequestId()))
//                    .forUpdate()
//                    .fetchOne();
//
//            if (outbox == null) {
//                return;
//            }
//            if (outbox.value4() != DispatchOutboxType.RESPONSE) {
//                return;
//            }
//            if (outbox.value5() == DispatchStatus.ACKED) {
//                return;
//            }
//            if (outbox.value5() != DispatchStatus.SENT && outbox.value5() != DispatchStatus.RETRY) {
//                return;
//            }
//
//            AnalysisStatus nextStatus = mapStatus(payload.status());
//            Long analysisId = upsertAnalysis(payload, outbox.value3());
//
//            //분석 완료된 상태
//            if (nextStatus == AnalysisStatus.COMPLETED) {
//                replaceKeywordMappings(analysisId, payload.keywordCounts());
//            }
//
//            dsl.update(ANALYSIS_DISPATCH_OUTBOX)
//                    .set(ANALYSIS_DISPATCH_OUTBOX.DISPATCH_STATUS, DispatchStatus.ACKED)
//                    .set(ANALYSIS_DISPATCH_OUTBOX.ANALYSIS_STATUS, nextStatus)
//                    .set(ANALYSIS_DISPATCH_OUTBOX.LAST_ERROR, payload.error())
//                    .set(ANALYSIS_DISPATCH_OUTBOX.CLAIMED_DONE_AT, currentLocalDateTime())
//                    .set(ANALYSIS_DISPATCH_OUTBOX.UPDATED_AT, currentLocalDateTime())
//                    .where(ANALYSIS_DISPATCH_OUTBOX.REQUEST_ID.eq(payload.dispatchRequestId()))
//                    .execute();
//
//            logChunkProgress(outbox.value2());
//        } catch (RuntimeException e) {
//            //재시도 처리는 트랜잭션 분리 - 부모 트핸잭션
//            retryService.markRetry(payload.dispatchRequestId(), e.getMessage());
//            throw e;
//        }
//    }
//
//    /**
//     * 수신 문자열 상태를 내부 Enum 상태로 변환
//     */
//    private AnalysisStatus mapStatus(String status) {
//        return switch (status) {
//            case "COMPLETED" -> AnalysisStatus.COMPLETED;
//            case "FAILED" -> AnalysisStatus.FAILED;
//            case "IN_PROGRESS" -> AnalysisStatus.IN_PROGRESS;
//            default -> AnalysisStatus.READY;
//        };
//    }
//
//    /**
//     * 상담 분석 테이블 upsert
//     */
//    private Long upsertAnalysis(
//            AnalysisResponseCommand payload,
//            Long jobInstanceId
//    ) {
//        List<AnalysisUpsertRow> single = List.of(
//                new AnalysisUpsertRow(
//                        payload.caseId(),
//                        jobInstanceId,
//                        payload.analyzerVersion()
//                )
//        );
//        Map<AnalysisKey, Long> upserted = bulkUpsertAnalyses(single);
//        //상담 FK + 분석기 버전(UNIQUE) 복합 키로 분석내역 PK 추출
//        Long analysisId = upserted.get(new AnalysisKey(payload.caseId(), payload.analyzerVersion()));
//        if (analysisId == null) {
//            //UPSERT 실패
//            throw new IllegalStateException("consultation_analysis upsert failed");
//        }
//        return analysisId;
//    }
//
//    /**
//     * (case_id, analyzer_version) 기준으로 consultation_analysis 다건 upsert
//     */
//    private Map<AnalysisKey, Long> bulkUpsertAnalyses(List<AnalysisUpsertRow> rows) {
//        if (rows == null || rows.isEmpty()) {
//            return Map.of();
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        //(상담Id, JobId, 분석기 version, created_at, updated_at)
//        List<Row5<Long, Long, Long, LocalDateTime, LocalDateTime>> valueRows =
//                new ArrayList<>(rows.size());
//        for (AnalysisUpsertRow rowValue : rows) {
//            valueRows.add(row(
//                    rowValue.caseId(),
//                    rowValue.jobInstanceId(),
//                    rowValue.analyzerVersion(),
//                    now,
//                    now
//            ));
//        }
//
//        Result<?> result = dsl.insertInto(
//                        CONSULTATION_ANALYSIS,
//                        CONSULTATION_ANALYSIS.CASE_ID,
//                        CONSULTATION_ANALYSIS.JOB_INSTANCE_ID,
//                        CONSULTATION_ANALYSIS.ANALYZER_VERSION,
//                        CONSULTATION_ANALYSIS.CREATED_AT,
//                        CONSULTATION_ANALYSIS.UPDATED_AT
//                )
//                .valuesOfRows(valueRows)
//                //제약조건
//                .onConflict(CONSULTATION_ANALYSIS.CASE_ID, CONSULTATION_ANALYSIS.ANALYZER_VERSION)
//                .doUpdate()
//                .set(CONSULTATION_ANALYSIS.JOB_INSTANCE_ID, excluded(CONSULTATION_ANALYSIS.JOB_INSTANCE_ID))
//                .set(CONSULTATION_ANALYSIS.UPDATED_AT, now)
//                .returning(
//                        //분석 내역 PK
//                        CONSULTATION_ANALYSIS.ANALYSIS_ID,
//                        //상담 Id
//                        CONSULTATION_ANALYSIS.CASE_ID,
//                        //분석기 버전
//                        CONSULTATION_ANALYSIS.ANALYZER_VERSION
//                )
//                .fetch();
//
//        Map<AnalysisKey, Long> upserted = new HashMap<>(result.size());
//        result.forEach(record -> {
//            AnalysisKey key = new AnalysisKey(
//                    record.get(CONSULTATION_ANALYSIS.CASE_ID),
//                    record.get(CONSULTATION_ANALYSIS.ANALYZER_VERSION)
//            );
//            //분석 내역 Id
//            upserted.put(key, record.get(CONSULTATION_ANALYSIS.ANALYSIS_ID));
//        });
//        return upserted;
//    }
//
//    /**
//     * 완료 상태: 키워드 매핑 결과를 교체 저장
//     */
//    private void replaceKeywordMappings(Long analysisId, List<AnalysisResponseCommand.KeywordCountCommand> keywordCounts) {
//        dsl.deleteFrom(BUSINESS_KEYWORD_MAPPING_RESULT)
//                .where(BUSINESS_KEYWORD_MAPPING_RESULT.ANALYSIS_ID.eq(analysisId))
//                .execute();
//
//        if (keywordCounts == null || keywordCounts.isEmpty()) {
//            return;
//        }
//
//        Map<Long, Integer> aggregated = new HashMap<>();
//        for (AnalysisResponseCommand.KeywordCountCommand item : keywordCounts) {
//            Long businessKeywordId = item.businessKeywordId() != null ? item.businessKeywordId() : item.keywordId();
//            if (businessKeywordId == null) {
//                continue;
//            }
//            int countValue = item.count() != null ? item.count() : 0;
//            aggregated.merge(businessKeywordId, countValue, Integer::sum);
//        }
//
//        if (aggregated.isEmpty()) {
//            return;
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        List<Row6<Long, Long, Integer, BigDecimal, LocalDateTime, LocalDateTime>> valueRows =
//                new ArrayList<>(aggregated.size());
//        for (Map.Entry<Long, Integer> entry : aggregated.entrySet()) {
//            valueRows.add(row(
//                    analysisId,
//                    entry.getKey(),
//                    entry.getValue(),
//                    BigDecimal.ZERO,
//                    now,
//                    now
//            ));
//        }
//
//        dsl.insertInto(
//                        BUSINESS_KEYWORD_MAPPING_RESULT,
//                        BUSINESS_KEYWORD_MAPPING_RESULT.ANALYSIS_ID,
//                        BUSINESS_KEYWORD_MAPPING_RESULT.BUSINESS_KEYWORD_ID,
//                        BUSINESS_KEYWORD_MAPPING_RESULT.COUNT,
//                        BUSINESS_KEYWORD_MAPPING_RESULT.CHANGE_RATE,
//                        BUSINESS_KEYWORD_MAPPING_RESULT.CREATED_AT,
//                        BUSINESS_KEYWORD_MAPPING_RESULT.UPDATED_AT
//                )
//                .valuesOfRows(valueRows)
//                .execute();
//    }
//
//    /**
//     * 같은 chunk_id 내 ACK 진행률을 로그
//     */
//    private void logChunkProgress(String chunkId) {
//        if (chunkId == null || chunkId.isBlank()) {
//            return;
//        }
//        var progress = dsl.select(
//                        count().as("total"),
//                        count().filterWhere(ANALYSIS_DISPATCH_OUTBOX.DISPATCH_STATUS.eq(DispatchStatus.ACKED)).as("acked")
//                )
//                .from(ANALYSIS_DISPATCH_OUTBOX)
//                .where(ANALYSIS_DISPATCH_OUTBOX.TYPE.eq(DispatchOutboxType.RESPONSE))
//                .and(ANALYSIS_DISPATCH_OUTBOX.CHUNK_ID.eq(chunkId))
//                .fetchOne();
//        if (progress == null) {
//            return;
//        }
//        long total = progress.get("total", Long.class);
//        long acked = progress.get("acked", Long.class);
//        log.info("[Kafka][analysis-response] chunk={} progress {}/{}", chunkId, acked, total);
//    }
//
//    /**
//     * payload의 필드와 스키마 검증
//     */
//    private void validate(AnalysisResponseCommand payload) {
//        if (!supportedSchema.equals(payload.schema())) {
//            throw new IllegalStateException("unsupported schema: " + payload.schema());
//        }
//
//        if (payload.dispatchRequestId() == null
//                || payload.caseId() == null
//                || payload.analyzerVersion() == null
//                || payload.status() == null) {
//            throw new IllegalStateException("field missing");
//        }
//    }
//
//    /**
//     * bulk upsert DTO
//     */
//    private record AnalysisUpsertRow(
//            Long caseId,
//            Long jobInstanceId,
//            Long analyzerVersion
//    ) {
//    }
//
//    /**
//     * bulk upsert 결과 매핑용
//     */
//    private record AnalysisKey(
//            Long caseId,
//            Long analyzerVersion
//    ) {
//    }
//}
