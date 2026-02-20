package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.web.dto.member.AdminMemberUpdateRequestDto;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.util.EncryptionTool;
import site.holliverse.admin.query.jooq.enums.MemberStatusType;
import site.holliverse.admin.query.jooq.enums.MemberMembershipType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateMemberUseCaseTest {

    @InjectMocks
    private UpdateMemberUseCase updateMemberUseCase;

    @Mock
    private AdminMemberDao adminMemberDao;

    @Mock
    private EncryptionTool encryptionTool;

    @Test
    @DisplayName("존재하지 않는 회원을 수정하려고 하면 예외가 발생한다")
    void throwExceptionWhenMemberNotFound() {
        // given
        Long memberId = 999L;
        AdminMemberUpdateRequestDto dto = new AdminMemberUpdateRequestDto("김길동", null, null, null);

        // 가짜 DB 동작 설정: 해당 회원은 없다고 응답함
        given(adminMemberDao.existsById(memberId)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            updateMemberUseCase.execute(memberId, dto);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(exception.getField()).isEqualTo("memberId");

        // Dao의 update 메서드는 절대 호출되지 않아야 함
        verify(adminMemberDao, never()).updateMember(anyLong(), anyString(), anyString(), any(), any());
    }

    @Test
    @DisplayName("입력된 필드만 정상적으로 암호화 및 업데이트된다")
    void updateMemberSuccessfully_Partial() {
        // given
        Long memberId = 1L;
        // 이름과 등급만 입력, 전화번호와 상태는 null
        AdminMemberUpdateRequestDto dto = new AdminMemberUpdateRequestDto("김길동", null, null, "VIP");

        given(adminMemberDao.existsById(memberId)).willReturn(true);
        given(encryptionTool.encrypt("김길동")).willReturn("encrypted_김길동");

        // when
        updateMemberUseCase.execute(memberId, dto);

        // then
        // 1. 이름은 값이 있으므로 암호화 메서드가 1번 호출되어야 함
        verify(encryptionTool, times(1)).encrypt("김길동");
        // 2. 전화번호는 null이므로 암호화 메서드가 호출되지 않아야 함
        verify(encryptionTool, never()).encrypt(isNull());

        // 3. Dao의 updateMember가 잘 호출되었는지 검증
        verify(adminMemberDao, times(1)).updateMember(
                memberId,
                "encrypted_김길동", // 암호화된 이름
                null,              // 폰 번호 (입력 안 했으니 null)
                null,              // 상태 (입력 안 했으니 null)
                MemberMembershipType.VIP        // 멤버십
        );
    }

    @Test
    @DisplayName("모든 필드가 입력되면 모두 처리되어 업데이트된다")
    void updateMemberSuccessfully_AllFields() {
        // given
        Long memberId = 2L;
        AdminMemberUpdateRequestDto dto = new AdminMemberUpdateRequestDto("홍길동", "01012345678", "BANNED", "GOLD");

        given(adminMemberDao.existsById(memberId)).willReturn(true);
        given(encryptionTool.encrypt("홍길동")).willReturn("encrypted_홍길동");
        given(encryptionTool.encrypt("01012345678")).willReturn("encrypted_01012345678");

        // when
        updateMemberUseCase.execute(memberId, dto);

        // then
        verify(adminMemberDao, times(1)).updateMember(
                memberId,
                "encrypted_홍길동",
                "encrypted_01012345678",
                MemberStatusType.BANNED,
                MemberMembershipType.GOLD
        );
    }
}