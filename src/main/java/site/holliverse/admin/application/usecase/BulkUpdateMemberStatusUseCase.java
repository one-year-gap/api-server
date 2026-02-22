package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.query.jooq.enums.MemberStatusType;
import site.holliverse.admin.web.dto.member.AdminMemberBulkStatusUpdateRequestDto;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

@Service
@RequiredArgsConstructor
public class BulkUpdateMemberStatusUseCase {

    private final AdminMemberDao adminMemberDao;

    @Transactional
    public int execute(AdminMemberBulkStatusUpdateRequestDto requestDto) {

        if (CollectionUtils.isEmpty(requestDto.memberIds())) {
            return 0; // 업데이트할 사람 0명
        }

        MemberStatusType targetStatus;
        try {
            targetStatus = MemberStatusType.valueOf(requestDto.status());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "status", "유효하지 않은 회원 상태값입니다.");
        }

        int updatedCount = adminMemberDao.updateMembersStatus(requestDto.memberIds(), targetStatus);

        return updatedCount;
    }
}