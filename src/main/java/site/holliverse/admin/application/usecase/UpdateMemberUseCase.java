package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.web.dto.member.AdminMemberUpdateRequestDto;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.util.EncryptionTool;

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

        // 4. Dao 호출
        adminMemberDao.updateMember(
                memberId,
                encryptedName,
                encryptedPhone,
                dto.status(),
                dto.membership()
        );
    }
}