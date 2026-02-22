package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.query.dao.MemberDetailRawData;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

/**
 * 관리자 - 회원 상세 정보 조회 UseCase
 */
@Profile("admin")
@Service
@RequiredArgsConstructor
public class GetMemberDetailUseCase {

    private final AdminMemberDao adminMemberDao;

    /**
     * 회원 상세 정보를 조회
     * @param memberId 조회할 회원의 고유 ID
     * @return MemberDetailRawData (DB에서 꺼낸 순수 데이터)
     */
    @Transactional(readOnly = true)
    public MemberDetailRawData execute(Long memberId) {

        // Dao를 통해 데이터 조회. 없으면 팀 표준 CustomException(404 NOT_FOUND) 발생
        return adminMemberDao.findDetailById(memberId)
                .orElseThrow(() -> new CustomException(
                ErrorCode.NOT_FOUND,
                "memberId", // 에러가 발생한 필드
                "해당 회원을 찾을 수 없습니다. (ID: " + memberId + ")" // 상세 이유
        ));
    }
}