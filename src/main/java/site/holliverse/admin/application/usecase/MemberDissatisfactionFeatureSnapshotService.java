package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.admin.application.usecase.dto.AnalysisResponseCommand;
import site.holliverse.admin.domain.model.churn.ChurnFeatureContribution;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.ChurnScoreCalculationResult;
import site.holliverse.admin.domain.model.churn.ConsultationSentimentType;
import site.holliverse.admin.domain.model.churn.feature.MemberDissatisfactionFeature;
import site.holliverse.admin.query.dao.MemberDissatisfactionFeatureSnapshotDao;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Profile("admin")
@RequiredArgsConstructor
public class MemberDissatisfactionFeatureSnapshotService {

    private final MemberDissatisfactionFeatureSnapshotDao snapshotDao;

    /**
     * feature 동기화.
     */
    public void sync(
            Long memberId,
            AnalysisResponseCommand command,
            MemberDissatisfactionFeature feature,
            ChurnScoreCalculationResult scoreResult
    ) {
        long snapshotId = snapshotDao.getOrCreateSnapshotId(memberId);

        snapshotDao.syncSnapshot(
                snapshotId,
                resolveFeatureScore(scoreResult),
                feature.starMeanScore(),
                resolveNegativeCounselIncrement(command),
                resolveNegativeKeywordCounts(command)
        );
    }

    /**
     * feature 점수.
     */
    private int resolveFeatureScore(ChurnScoreCalculationResult scoreResult) {
        return scoreResult.contributions().stream()
                .filter(contribution -> contribution.featureType() == ChurnFeatureType.MEMBER_DISSATISFACTION)
                .mapToInt(ChurnFeatureContribution::appliedScore)
                .sum();
    }

    /**
     * 부정 상담 증분.
     */
    private int resolveNegativeCounselIncrement(AnalysisResponseCommand command) {
        return command.consultationType() == ConsultationSentimentType.NEGATIVE ? 1 : 0;
    }

    /**
     * 부정 키워드 집계.
     */
    private Map<String, Integer> resolveNegativeKeywordCounts(AnalysisResponseCommand command) {
        Map<String, Integer> keywordCounts = new LinkedHashMap<>();
        if (command.keywordCounts() == null) {
            return keywordCounts;
        }

        command.keywordCounts().stream()
                .filter(keyword -> keyword.negativeWeight() != null && keyword.negativeWeight() > 0)
                .forEach(keyword -> keywordCounts.merge(
                        resolveKeywordName(keyword),
                        keyword.count() != null ? keyword.count() : 0,
                        Integer::sum
                ));

        return keywordCounts;
    }

    /**
     * 키워드 이름.
     */
    private String resolveKeywordName(AnalysisResponseCommand.KeywordCountCommand keyword) {
        if (keyword.keywordName() != null && !keyword.keywordName().isBlank()) {
            return keyword.keywordName();
        }
        if (keyword.keywordCode() != null && !keyword.keywordCode().isBlank()) {
            return keyword.keywordCode();
        }
        return String.valueOf(keyword.businessKeywordId());
    }
}
