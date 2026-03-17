package site.holliverse.admin.application.usecase.dto;

import site.holliverse.admin.domain.model.churn.ConsultationSentimentType;

import java.time.Instant;
import java.util.List;

public record AnalysisResponseCommand(
        String schema,
        String dispatchRequestId,
        String chunkId,
        Long caseId,
        Long analyzerVersion,
        Long analysisId,
        Long memberId,
        String status,
        ConsultationSentimentType consultationType,
        Integer keywordTypes,
        Integer keywordHits,
        List<KeywordCountCommand> keywordCounts,
        String error,
        Instant producedAt
) {
    public record KeywordCountCommand(
            Long keywordId,
            Long businessKeywordId,
            String keywordCode,
            String keywordName,
            Integer count,
            Integer negativeWeight
    ) {
    }
}
