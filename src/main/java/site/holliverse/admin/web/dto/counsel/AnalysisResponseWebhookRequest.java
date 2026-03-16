package site.holliverse.admin.web.dto.counsel;

import java.time.Instant;
import java.util.List;

public record AnalysisResponseWebhookRequest(
        String dispatchRequestId,
        long caseId,
        int analyzeVersion,
        int analysisId,
        long memberId,
        CounselAnalysisStatus status,
        int keywordTypes,
        int keywordHits,
        CounselEmotionType consultationType,
        List<KeywordResult> keywordCounts,
        Instant producedAt
) {
}
