package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.MemberActionFeatureLogDao;
import site.holliverse.admin.web.dto.log.LogFeaturesRequestDto;

/**
 * POST /api/v1/admin/log-features 처리.
 * 스냅샷 조회/생성 정책: 해당 회원의 MEMBER_ACTION_FEATURE 최신 스냅샷을 쓰고, 없으면 1건 생성 후
 * comparison_cnt / checked_penalty_fee_cnt 만 증분 갱신.
 */
@Service
@Profile("admin")
@RequiredArgsConstructor
@Slf4j
public class LogFeaturesUseCase {

    private final MemberActionFeatureLogDao memberActionFeatureLogDao;

    /**
     * 요청대로 member_action_feature 카운트만 증분 반영.
     * 스냅샷이 없으면 정책에 따라 생성 후 갱신.
     */
    @Transactional
    public void execute(LogFeaturesRequestDto request) {
        long snapshotId = memberActionFeatureLogDao.getOrCreateSnapshotId(request.getMemberId());
        int updated = memberActionFeatureLogDao.incrementCounts(
                snapshotId,
                request.getComparisonIncrement(),
                request.getPenaltyIncrement()
        );
        if (updated > 0) {
            log.debug("log-features: member_id={}, snapshot_id={}, comparison+{}, penalty+{}",
                    request.getMemberId(), snapshotId,
                    request.getComparisonIncrement(), request.getPenaltyIncrement());
        }
    }
}
