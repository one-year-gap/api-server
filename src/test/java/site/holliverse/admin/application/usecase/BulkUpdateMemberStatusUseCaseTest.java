package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.query.jooq.enums.MemberStatusType;
import site.holliverse.admin.web.dto.member.AdminMemberBulkStatusUpdateRequestDto;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BulkUpdateMemberStatusUseCaseTest {

    @InjectMocks
    private BulkUpdateMemberStatusUseCase useCase;

    @Mock
    private AdminMemberDao adminMemberDao;

    @Test
    @DisplayName("성공: 유효한 ID 리스트와 상태값이 주어지면 DAO를 호출하여 업데이트 건수를 반환한다.")
    void execute_success() {
        // given
        List<Long> memberIds = List.of(1L, 2L, 3L);
        String statusStr = "BANNED";
        AdminMemberBulkStatusUpdateRequestDto requestDto = new AdminMemberBulkStatusUpdateRequestDto(memberIds, statusStr);

        // DAO가 3명을 성공적으로 업데이트했다고 가정 (Mocking)
        given(adminMemberDao.updateMembersStatus(eq(memberIds), eq(MemberStatusType.BANNED)))
                .willReturn(3);

        // when
        int result = useCase.execute(requestDto);

        // then
        assertThat(result).isEqualTo(3); // 반환값이 3인지 검증
        verify(adminMemberDao).updateMembersStatus(eq(memberIds), eq(MemberStatusType.BANNED)); // DAO가 호출되었는지 검증
    }

    @Test
    @DisplayName("성공(조기종료): 회원 ID 리스트가 비어있으면 DAO를 호출하지 않고 0을 반환한다.")
    void execute_empty_list_returns_zero() {
        // given
        List<Long> emptyIds = Collections.emptyList();
        // 상태값은 정상이지만 리스트가 빈 상황
        AdminMemberBulkStatusUpdateRequestDto requestDto = new AdminMemberBulkStatusUpdateRequestDto(emptyIds, "BANNED");

        // when
        int result = useCase.execute(requestDto);

        // then
        assertThat(result).isEqualTo(0); // 0 반환 확인
        verify(adminMemberDao, never()).updateMembersStatus(any(), any()); // DAO가 호출되지 않았음을 검증
    }

    @Test
    @DisplayName("실패: 유효하지 않은 상태값이 들어오면 CustomException(400)이 발생한다.")
    void execute_fail_invalid_status_enum() {
        // given
        List<Long> memberIds = List.of(1L);
        String invalidStatus = "INVALID_STATUS"; // 존재하지 않는 상태값
        AdminMemberBulkStatusUpdateRequestDto requestDto = new AdminMemberBulkStatusUpdateRequestDto(memberIds, invalidStatus);

        // when & then
        assertThatThrownBy(() -> useCase.execute(requestDto))
                .isInstanceOf(CustomException.class) // CustomException 발생 여부
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT); // 에러 코드 검증

        // 예외가 발생했으므로 DAO는 호출되면 안 됨
        verify(adminMemberDao, never()).updateMembersStatus(any(), any());
    }
}