package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.MemberActionFeatureLogDao;
import site.holliverse.admin.web.dto.log.LogFeaturesRequestDto;

import java.util.Optional;

/**
 * POST /api/v1/admin/log-features 처리.
 * 해당 회원의 MEMBER_ACTION_FEATURE 최신 스냅샷이 있으면 comparison_cnt / checked_penalty_fee_cnt 만 증분 갱신.
 * 스냅샷이 없으면 no-op (배치 미실행 등으로 스냅샷이 아직 없을 수 있음).
 */
@Service
@Profile("admin")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LogFeaturesUseCase {

    private final MemberActionFeatureLogDao memberActionFeatureLogDao;

    /**
     * 요청대로 member_action_feature 카운트만 증분 반영. 스냅샷 없으면 무시.
     */
    public void execute(LogFeaturesRequestDto request) {
        Optional<Long> snapshotIdOpt = memberActionFeatureLogDao.findLatestSnapshotId(request.getMemberId());
        if (snapshotIdOpt.isEmpty()) {
            log.debug("log-features: member_id={} 에 대한 MEMBER_ACTION_FEATURE 스냅샷 없음, no-op", request.getMemberId());
            return;
        }
        int updated = memberActionFeatureLogDao.incrementCounts(
                snapshotIdOpt.get(),
                request.getComparisonIncrement(),
                request.getPenaltyIncrement()
        );
        if (updated > 0) {
            log.debug("log-features: member_id={}, snapshot_id={}, comparison+{}, penalty+{}",
                    request.getMemberId(), snapshotIdOpt.get(),
                    request.getComparisonIncrement(), request.getPenaltyIncrement());
        }
    }
}
