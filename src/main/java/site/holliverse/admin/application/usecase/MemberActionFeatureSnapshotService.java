package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.admin.domain.model.churn.ChurnFeatureContribution;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.ChurnScoreCalculationResult;
import site.holliverse.admin.domain.model.churn.feature.MemberActionFeature;
import site.holliverse.admin.query.dao.MemberActionFeatureLogDao;

import java.util.List;

/**
 * 로그 feature 스냅샷 서비스.
 */
@Service
@Profile("admin")
@RequiredArgsConstructor
public class MemberActionFeatureSnapshotService {

    private final MemberActionFeatureLogDao snapshotDao;

    /**
     * 스냅샷 준비.
     */
    public SnapshotContext prepare(Long memberId, List<LogFeatureEvent> events) {
        MemberActionFeatureLogDao.ActionSnapshot snapshot = snapshotDao.getOrCreateSnapshot(memberId);

        MemberActionFeature updatedFeature = new MemberActionFeature(
                snapshot.feature().changeMobileCount(),
                snapshot.feature().comparisonCount() + count(events, LogFeatureEventName.CLICK_COMPARE),
                snapshot.feature().checkedPenaltyFeeCount() + count(events, LogFeatureEventName.CLICK_PENALTY)
        );

        return new SnapshotContext(snapshot.snapshotId(), updatedFeature);
    }

    /**
     * 스냅샷 동기화.
     */
    public void sync(SnapshotContext context, ChurnScoreCalculationResult scoreResult) {
        snapshotDao.syncSnapshot(
                context.snapshotId(),
                resolveFeatureScore(scoreResult),
                context.feature()
        );
    }

    /**
     * feature 점수.
     */
    private int resolveFeatureScore(ChurnScoreCalculationResult scoreResult) {
        return scoreResult.contributions().stream()
                .filter(contribution -> contribution.featureType() == ChurnFeatureType.MEMBER_ACTION)
                .mapToInt(ChurnFeatureContribution::appliedScore)
                .sum();
    }

    /**
     * 이벤트 개수.
     */
    private int count(List<LogFeatureEvent> events, LogFeatureEventName eventName) {
        return (int) events.stream()
                .filter(event -> event.eventName() == eventName)
                .count();
    }

    /**
     * 스냅샷 문맥.
     */
    public record SnapshotContext(
            Long snapshotId,
            MemberActionFeature feature
    ) {
    }
}
