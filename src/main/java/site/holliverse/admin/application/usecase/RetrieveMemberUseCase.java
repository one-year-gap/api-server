package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.query.dao.MemberRawData;
import site.holliverse.admin.web.dto.member.AdminMemberListRequestDto;

import java.util.List;

/**
 * 관리자 - 회원 목록 조회 UseCase
 */
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

        // 1. DAO를 통해 조건에 맞는 회원 목록(암호화된 상태) 조회
        // DAO 내부에서 이미 검색어 암호화 로직이 있으므로 그대로 호출
        List<MemberRawData> members = adminMemberDao.findAll(requestDto);

        // 2. 페이징 계산을 위한 전체 데이터 개수 조회
        long totalCount = adminMemberDao.count(requestDto);

        // 3. 수집된 데이터를 결과 객체에 담아 반환 (Web 계층으로 배달)
        return new RetrieveMemberResult(members, (int) totalCount);
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