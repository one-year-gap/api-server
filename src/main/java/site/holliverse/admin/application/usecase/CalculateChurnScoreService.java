package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.admin.application.usecase.dto.AnalysisResponseCommand;
import site.holliverse.admin.domain.model.churn.ChurnEvaluationResult;
import site.holliverse.admin.domain.model.churn.ChurnFeatureSet;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.ChurnSignalType;
import site.holliverse.admin.domain.model.churn.ChurnScoreCalculationResult;
import site.holliverse.admin.domain.model.churn.feature.MemberDissatisfactionFeature;
import site.holliverse.admin.domain.policy.churn.ChurnScorePolicy;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Profile("admin")
@RequiredArgsConstructor
public class CalculateChurnScoreService {

    private final ChurnScorePolicy churnScorePolicy;
    private final ChurnRiskReasonFactory churnRiskReasonFactory;
    private final ChurnSnapshotStoreService churnSnapshotStoreService;
    private final MemberDissatisfactionFeatureSnapshotService memberDissatisfactionFeatureSnapshotService;

    /**
     * 스냅샷 계산.
     */
    public ChurnEvaluationResult calculateAndStore(
            AnalysisResponseCommand command,
            LocalDate baseDate,
            MemberDissatisfactionFeature dissatisfactionFeature
    ) {
        // feature 조립
        ChurnFeatureSet featureSet = new ChurnFeatureSet(Map.of(
                ChurnFeatureType.MEMBER_DISSATISFACTION,
                dissatisfactionFeature
        ));

        // 점수 계산
        ChurnScoreCalculationResult scoreResult = churnScorePolicy.calculateDetails(featureSet);

        // feature 스냅샷 저장
        memberDissatisfactionFeatureSnapshotService.sync(
                command.memberId(),
                command,
                dissatisfactionFeature,
                scoreResult
        );

        // 위험 사유 조립
        List<ChurnRiskReason> riskReasons = buildCounselRiskReasons(command, scoreResult);

        // 스냅샷 저장
        return churnSnapshotStoreService.store(
                command.memberId(),
                baseDate,
                ChurnRiskReason.Feature.COUNSEL,
                riskReasons
        );
    }

    /**
     * 상담 사유 조립.
     */
    private List<ChurnRiskReason> buildCounselRiskReasons(
            AnalysisResponseCommand command,
            ChurnScoreCalculationResult scoreResult
    ) {
        return java.util.stream.Stream.of(
                        buildSentimentReason(command, scoreResult),
                        buildKeywordReason(command, scoreResult)
                )
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * 감정 사유.
     */
    private Optional<ChurnRiskReason> buildSentimentReason(
            AnalysisResponseCommand command,
            ChurnScoreCalculationResult scoreResult
    ) {
        if (command.consultationType() == null) {
            return Optional.empty();
        }

        return churnRiskReasonFactory.create(
                scoreResult,
                ChurnRiskReason.Feature.COUNSEL,
                ChurnRiskReason.ReasonCode.NEGATIVE_SENTIMENT,
                ChurnSignalType.CONSULTATION_SENTIMENT,
                command.consultationType().name(),
                ChurnRiskReason.ReasonCode.NEGATIVE_SENTIMENT.sentimentSummary(command.consultationType().name()),
                new ChurnRiskReason.SentimentEvidence(
                        command.caseId(),
                        command.consultationType().name()
                )
        );
    }

    /**
     * 키워드 사유.
     */
    private Optional<ChurnRiskReason> buildKeywordReason(
            AnalysisResponseCommand command,
            ChurnScoreCalculationResult scoreResult
    ) {
        if (command.keywordCounts() == null || command.keywordCounts().isEmpty()) {
            return Optional.empty();
        }

        List<ChurnRiskReason.KeywordItem> keywords = command.keywordCounts().stream()
                .filter(keyword -> keyword.negativeWeight() != null && keyword.negativeWeight() > 0)
                .sorted(Comparator
                        .comparing(AnalysisResponseCommand.KeywordCountCommand::negativeWeight, Comparator.reverseOrder())
                        .thenComparing(AnalysisResponseCommand.KeywordCountCommand::count, Comparator.reverseOrder()))
                .map(keyword -> new ChurnRiskReason.KeywordItem(
                        keyword.businessKeywordId(),
                        keyword.keywordName(),
                        keyword.count(),
                        keyword.negativeWeight()
                ))
                .toList();

        if (keywords.isEmpty()) {
            return Optional.empty();
        }

        return churnRiskReasonFactory.create(
                scoreResult,
                ChurnRiskReason.Feature.COUNSEL,
                ChurnRiskReason.ReasonCode.KEYWORD,
                ChurnSignalType.MAX_KEYWORD_NEGATIVE_WEIGHT,
                keywords.get(0).negativeWeight(),
                ChurnRiskReason.ReasonCode.KEYWORD.keywordSummary(keywords),
                new ChurnRiskReason.KeywordEvidence(keywords)
        );
    }
}
