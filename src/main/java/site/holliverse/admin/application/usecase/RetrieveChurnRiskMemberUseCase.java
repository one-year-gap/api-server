package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.ChurnRiskMemberDao;
import site.holliverse.admin.query.dao.ChurnRiskMemberRawData;
import site.holliverse.admin.web.dto.churn.ChurnRiskMemberListRequestDto;

import java.util.Collections;
import java.util.List;

/**
 * 이탈 위험군 목록 조회 UseCase.
 *
 * 역할
 * 1. 전체 건수 조회
 * 2. 데이터가 없으면 빈 목록 즉시 반환
 * 3. 데이터가 있으면 실제 목록 조회
 *
 */
@Profile("admin")
@Service
@RequiredArgsConstructor
public class RetrieveChurnRiskMemberUseCase {

    private final ChurnRiskMemberDao churnRiskMemberDao;

    /**
     * 화면 조회 요청을 받아 목록 + totalCount를 묶어서 반환
     */
    @Transactional(readOnly = true)
    public RetrieveChurnRiskMemberResult execute(ChurnRiskMemberListRequestDto requestDto) {
        int totalCount = (int) churnRiskMemberDao.count(requestDto);

        // count가 0이면 불필요한 목록 쿼리를 한 번 더 날리지 않는다.
        if (totalCount == 0) {
            return new RetrieveChurnRiskMemberResult(Collections.emptyList(), 0);
        }

        List<ChurnRiskMemberRawData> members = churnRiskMemberDao.findAll(requestDto);
        return new RetrieveChurnRiskMemberResult(members, totalCount);
    }

    /**
     * 가공전 Members 객체
     */
    public record RetrieveChurnRiskMemberResult(
            List<ChurnRiskMemberRawData> members,
            int totalCount
    ) {
    }
}
