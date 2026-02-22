package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.web.dto.member.AdminMemberUpdateRequestDto;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.util.EncryptionTool;

import site.holliverse.admin.query.jooq.enums.MemberStatusType;
import site.holliverse.admin.query.jooq.enums.MemberMembershipType;

@Profile("admin")
@Service
@RequiredArgsConstructor
public class UpdateMemberUseCase {

    private final AdminMemberDao adminMemberDao;
    private final EncryptionTool encryptionTool;

    @Transactional
    public void execute(Long memberId, AdminMemberUpdateRequestDto dto) {

        // 1. 대상 회원이 진짜 존재하는지 검증
        if (!adminMemberDao.existsById(memberId)) {
            // 커스텀 예외 적용
            throw new CustomException(ErrorCode.NOT_FOUND, "memberId", "존재하지 않는 회원입니다.");
        }

        // 2. 이름 암호화 (값이 들어왔을 때만)
        String encryptedName = StringUtils.hasText(dto.name()) ? encryptionTool.encrypt(dto.name()) : null;

        // 3. 전화번호 암호화 (값이 들어왔을 때만)
        String encryptedPhone = StringUtils.hasText(dto.phone()) ? encryptionTool.encrypt(dto.phone()) : null;

        // 4. 상태(Status) 변환 및 검증
        MemberStatusType statusEnum = null;
        if (StringUtils.hasText(dto.status())) {
            try {
                statusEnum = MemberStatusType.valueOf(dto.status());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "status", "유효하지 않은 회원 상태값입니다.");
            }
        }

        // 5: 멤버십(Membership) 변환 및 검증
        MemberMembershipType membershipEnum = null;
        if (StringUtils.hasText(dto.membership())) {
            try {
                membershipEnum = MemberMembershipType.valueOf(dto.membership());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "membership", "유효하지 않은 멤버십 등급입니다.");
            }
        }

        // 4. Dao 호출
        adminMemberDao.updateMember(
                memberId,
                encryptedName,
                encryptedPhone,
                statusEnum,
                membershipEnum
        );
    }
}