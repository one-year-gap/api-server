package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.application.usecase.dto.AnalysisResponseCommand;
import site.holliverse.admin.domain.model.churn.ConsultationSentimentType;
import site.holliverse.admin.domain.model.churn.feature.MemberDissatisfactionFeature;
import site.holliverse.admin.query.dao.ConsultationChurnSourceDao;

@Component
@Profile("admin")
@RequiredArgsConstructor
/**
 * 고객 불만 기반
 */
public class MemberDissatisfactionAssembler {

    private final ConsultationChurnSourceDao consultationChurnSourceDao;

    public MemberDissatisfactionFeature assemble(AnalysisResponseCommand command) {
        // 만족도 평균
        double starMeanScore = consultationChurnSourceDao.findAverageSatisfactionScore(command.memberId());

        // 상담 감정
        ConsultationSentimentType sentimentType = command.consultationType() != null
                ? command.consultationType()
                : ConsultationSentimentType.NONE;

        // 최대 키워드 가중치
        int maxKeywordNegativeWeight = command.keywordCounts() == null
                ? 0
                : command.keywordCounts().stream()
                .map(AnalysisResponseCommand.KeywordCountCommand::negativeWeight)
                .filter(weight -> weight != null && weight > 0)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        return new MemberDissatisfactionFeature(
                starMeanScore,
                sentimentType,
                maxKeywordNegativeWeight
        );
    }
}
