package site.holliverse.admin.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.admin.application.usecase.HandleAnalysisConsultationUseCase;
import site.holliverse.admin.application.usecase.dto.AnalysisResponseCommand;
import site.holliverse.admin.domain.model.churn.ConsultationSentimentType;
import site.holliverse.admin.web.dto.counsel.AnalysisResponseWebhookRequest;

import java.util.List;

@Profile("admin")
@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/v1/analysis-consultation")
public class InternalAnalysisWebhookController {
    private final HandleAnalysisConsultationUseCase useCase;

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody AnalysisResponseWebhookRequest request) {
        // 요청 처리
        useCase.execute(toCommand(request));

        // 응답 반환
        return ResponseEntity.accepted().build();
    }

    private AnalysisResponseCommand toCommand(AnalysisResponseWebhookRequest request) {
        // 키워드 목록 변환
        List<AnalysisResponseCommand.KeywordCountCommand> keywordCounts = request.keywordCounts() == null
                ? List.of()
                : request.keywordCounts().stream()
                .map(item -> new AnalysisResponseCommand.KeywordCountCommand(
                        item.keywordId(),
                        item.businessKeywordId(),
                        item.keywordCode(),
                        item.keywordName(),
                        item.count(),
                        item.negativeWeight()
                ))
                .toList();

        // 명령 변환
        return new AnalysisResponseCommand(
                "analysis.response.v1",
                request.dispatchRequestId(),
                null,
                request.caseId(),
                (long) request.analyzeVersion(),
                (long) request.analysisId(),
                request.memberId(),
                request.status().name(),
                toSentimentType(request.consultationType()),
                request.keywordTypes(),
                request.keywordHits(),
                keywordCounts,
                null,
                request.producedAt() != null ? request.producedAt() : java.time.Instant.now()
        );
    }

    private ConsultationSentimentType toSentimentType(Enum<?> consultationType) {
        // 기본 감정
        if (consultationType == null) {
            return ConsultationSentimentType.NONE;
        }

        // 감정 변환
        return ConsultationSentimentType.valueOf(consultationType.name());
    }
}
