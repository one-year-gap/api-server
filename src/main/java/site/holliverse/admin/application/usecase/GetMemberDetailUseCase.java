package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.error.AdminErrorCode;
import site.holliverse.admin.error.AdminException;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.query.dao.MemberDetailRawData;
import site.holliverse.shared.alert.AlertOwner;
import site.holliverse.shared.logging.SystemLogEvent;

import java.util.List;

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
     * @return GetMemberDetailResult (RawData + Top3 키워드)
     */
    @Transactional(readOnly = true)
    @SystemLogEvent("admin.member.detail")
    @AlertOwner("yh")
    public GetMemberDetailResult execute(Long memberId) {

        // 1. 기존 상세 정보 + 단순 통계 3개 조회
        MemberDetailRawData rawData = adminMemberDao.findDetailById(memberId)
                .orElseThrow(() -> new AdminException(AdminErrorCode.MEMBER_NOT_FOUND));

        // 2. 주요 상담 키워드 Top 3 별도 조회
        List<String> top3Keywords = adminMemberDao.findTop3KeywordsByMemberId(memberId);

        // 3. 두 데이터를 하나로 묶어서 반환
        return new GetMemberDetailResult(rawData, top3Keywords);
    }

    /**
     * UseCase의 결과를 묶어서 반환하기 위한 전용 Record
     */
    public record GetMemberDetailResult(
            MemberDetailRawData rawData,
            List<String> top3Keywords
    ) {}
}
