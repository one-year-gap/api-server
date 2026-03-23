package site.holliverse.admin.web.dto.counsel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;

public record AnalysisResponseWebhookRequest(
        @NotBlank(message = "dispatchRequestId는 필수입니다.")
        String dispatchRequestId,

        @Positive(message = "caseId는 1 이상이어야 합니다.")
        long caseId,

        @Positive(message = "analyzeVersion은 1 이상이어야 합니다.")
        int analyzeVersion,

        @Positive(message = "analysisId는 1 이상이어야 합니다.")
        int analysisId,

        @Positive(message = "memberId는 1 이상이어야 합니다.")
        long memberId,

        @NotNull(message = "status는 필수입니다.")
        CounselAnalysisStatus status,

        @Min(value = 0, message = "keywordTypes는 0 이상이어야 합니다.")
        int keywordTypes,

        @Min(value = 0, message = "keywordHits는 0 이상이어야 합니다.")
        int keywordHits,

        @NotNull(message = "consultationType은 필수입니다.")
        CounselEmotionType consultationType,

        @NotNull(message = "keywordCounts는 필수입니다.")
        @Valid
        List<@Valid KeywordResult> keywordCounts,

        @NotNull(message = "producedAt은 필수입니다.")
        Instant producedAt
) {
}
