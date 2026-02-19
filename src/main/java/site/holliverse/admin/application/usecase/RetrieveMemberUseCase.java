package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.query.dao.MemberRawData;
import site.holliverse.admin.web.dto.member.AdminMemberListRequestDto;

import java.util.Collections;
import java.util.List;

/**
 * 관리자 - 회원 목록 조회 UseCase
 */
@Profile("admin")
@Service
@RequiredArgsConstructor
public class RetrieveMemberUseCase {

    private final AdminMemberDao adminMemberDao;

    /**
     * 회원 목록 조회를 위한 데이터 수집 실행
     * @param requestDto 검색 조건 및 페이징 정보
     * @return DB에서 가져온 날것의 데이터(RawData)와 전체 카운트
     */
    @Transactional(readOnly = true) // 단순 조회의 경우 성능 최적화를 위해 읽기 전용 트랜잭션 사용
    public RetrieveMemberResult execute(AdminMemberListRequestDto requestDto) {
        // 1. 전체 개수를 먼저 조회
        int totalCount = (int) adminMemberDao.count(requestDto);

        // 데이터가 0건이면 굳이 목록 조회를 할 필요 X (성능 최적화)
        if (totalCount == 0) {
            return new RetrieveMemberResult(Collections.emptyList(), 0);
        }

        // 2. 데이터가 있을 때만 목록 조회 실행
        List<MemberRawData> members = adminMemberDao.findAll(requestDto);

        return new RetrieveMemberResult(members, totalCount);
    }

    /**
     * UseCase의 결과를 담는 단순 전송용 객체 (Record)
     * - 'members'는 아직 암호화된 상태이며, 마스킹도 적용되지 않은 Raw 데이터.
     */
    public record RetrieveMemberResult(
            List<MemberRawData> members,
            int totalCount
    ) {}
}