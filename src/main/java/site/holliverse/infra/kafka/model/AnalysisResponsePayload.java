//package site.holliverse.infra.kafka.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//
//import java.time.Instant;
//import java.util.List;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//public record AnalysisResponsePayload(
//        String schema,
//        String dispatchRequestId,
//        String chunkId,
//        //상담 id
//        Long caseId,
//        //상담 분석 알고리즘 id
//        Long analyzerVersion,
//        Long analysisId,
//        Long memberId,
//        String status,
//        Integer keywordTypes,
//        Integer keywordHits,
//        //분석 결과
//        List<KeywordCountItem> keywordCounts,
//        String error,
//        Instant producedAt
//) {
//}
