package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.admin.domain.model.churn.ChurnEvaluationResult;
import site.holliverse.admin.domain.model.churn.ChurnFeatureSet;
import site.holliverse.admin.domain.model.churn.ChurnFeatureType;
import site.holliverse.admin.domain.model.churn.ChurnScoreCalculationResult;
import site.holliverse.admin.domain.model.churn.ChurnSignalType;
import site.holliverse.admin.domain.model.churn.feature.MemberActionFeature;
import site.holliverse.admin.domain.policy.churn.ChurnScorePolicy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 로그 이탈 계산 서비스.
 */
@Service
@Profile("admin")
@RequiredArgsConstructor
public class CalculateLogChurnScoreService {

    private final ChurnScorePolicy churnScorePolicy;
    private final ChurnRiskReasonFactory churnRiskReasonFactory;
    private final ChurnSnapshotStoreService churnSnapshotStoreService;
    private final MemberActionFeatureSnapshotService memberActionFeatureSnapshotService;

    /**
     * 로그 스냅샷 계산.
     */
    public ChurnEvaluationResult calculateAndStore(
            Long memberId,
            LocalDate baseDate,
            List<LogFeatureEvent> events
    ) {
        // 스냅샷 준비
        MemberActionFeatureSnapshotService.SnapshotContext snapshotContext =
                memberActionFeatureSnapshotService.prepare(memberId, events);

        // feature 조립
        ChurnFeatureSet featureSet = new ChurnFeatureSet(Map.of(
                ChurnFeatureType.MEMBER_ACTION,
                snapshotContext.feature()
        ));

        // 점수 계산
        ChurnScoreCalculationResult scoreResult = churnScorePolicy.calculateDetails(featureSet);

        // feature 스냅샷 저장
        memberActionFeatureSnapshotService.sync(snapshotContext, scoreResult);

        // 위험 사유 조립
        List<ChurnRiskReason> riskReasons = buildLogRiskReasons(events, snapshotContext.feature(), scoreResult);

        // 스냅샷 저장
        return churnSnapshotStoreService.store(memberId, baseDate, ChurnRiskReason.Feature.LOG, riskReasons);
    }

    /**
     * 로그 사유 조립.
     */
    private List<ChurnRiskReason> buildLogRiskReasons(
            List<LogFeatureEvent> events,
            MemberActionFeature feature,
            ChurnScoreCalculationResult scoreResult
    ) {
        List<LogFeatureEvent> comparisonEvents = filter(events, LogFeatureEventName.CLICK_COMPARE);
        List<LogFeatureEvent> penaltyEvents = filter(events, LogFeatureEventName.CLICK_PENALTY);

        return java.util.stream.Stream.of(
                        buildLogReason(
                                comparisonEvents,
                                feature.comparisonCount(),
                                scoreResult,
                                ChurnRiskReason.ReasonCode.COMPARE,
                                ChurnSignalType.COMPARISON_COUNT
                        ),
                        buildLogReason(
                                penaltyEvents,
                                feature.checkedPenaltyFeeCount(),
                                scoreResult,
                                ChurnRiskReason.ReasonCode.CHECKED_PENALTY_FEE,
                                ChurnSignalType.CHECKED_PENALTY_FEE_COUNT
                        )
                )
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * 로그 사유.
     */
    private Optional<ChurnRiskReason> buildLogReason(
            List<LogFeatureEvent> events,
            int totalCount,
            ChurnScoreCalculationResult scoreResult,
            ChurnRiskReason.ReasonCode reasonCode,
            ChurnSignalType signalType
    ) {
        if (events.isEmpty()) {
            return Optional.empty();
        }

        return churnRiskReasonFactory.create(
                scoreResult,
                ChurnRiskReason.Feature.LOG,
                reasonCode,
                signalType,
                totalCount,
                reasonCode.logSummary(totalCount),
                new ChurnRiskReason.LogEvidence(
                        events.size(),
                        totalCount,
                        toLogItems(events)
                )
        );
    }

    /**
     * 이벤트 필터.
     */
    private List<LogFeatureEvent> filter(List<LogFeatureEvent> events, LogFeatureEventName eventName) {
        return events.stream()
                .filter(event -> event.eventName() == eventName)
                .toList();
    }

    /**
     * 이벤트 근거.
     */
    private List<ChurnRiskReason.LogEventItem> toLogItems(List<LogFeatureEvent> events) {
        return events.stream()
                .map(event -> new ChurnRiskReason.LogEventItem(
                        event.eventId(),
                        event.timestamp().toString(),
                        event.event(),
                        event.eventName().value(),
                        event.eventProperties()
                ))
                .toList();
    }
}
