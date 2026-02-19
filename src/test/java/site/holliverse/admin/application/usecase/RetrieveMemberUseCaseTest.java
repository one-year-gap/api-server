package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.query.dao.MemberRawData;
import site.holliverse.admin.web.dto.member.AdminMemberListRequestDto;
import site.holliverse.shared.util.EncryptionTool;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("admin")
@ExtendWith(MockitoExtension.class)
class RetrieveMemberUseCaseTest {

    @Mock
    private AdminMemberDao adminMemberDao;

    @InjectMocks
    private RetrieveMemberUseCase retrieveMemberUseCase;

    @Test
    @DisplayName("회원 데이터가 존재할 때, 전체 목록을 정상적으로 반환한다. (분기: totalCount > 0)")
    void execute_with_keyword_success() {
        // given
        AdminMemberListRequestDto req = new AdminMemberListRequestDto(1, 10, null, null, null, null, null, null, null);

        given(adminMemberDao.count(any())).willReturn(10L); // 10건 있다고 가정
        given(adminMemberDao.findAll(any())).willReturn(List.of(new MemberRawData()));

        // when
        RetrieveMemberUseCase.RetrieveMemberResult result = retrieveMemberUseCase.execute(req);

        // then
        assertThat(result.totalCount()).isEqualTo(10);
        assertThat(result.members()).isNotEmpty();
        verify(adminMemberDao, times(1)).findAll(any()); // 목록 조회가 실행되었는지 확인
    }

    @Test
    @DisplayName("회원 데이터가 없을 때, 목록 조회를 생략하고 빈 리스트를 반환한다. (분기: totalCount == 0)")
    void execute_without_keyword_success() {
        // given
        AdminMemberListRequestDto req = new AdminMemberListRequestDto(1, 10, null, null, null, null, null, null, null);

        given(adminMemberDao.count(any())).willReturn(0L); // 0건이라고 가정

        // when
        RetrieveMemberUseCase.RetrieveMemberResult result = retrieveMemberUseCase.execute(req);

        // then
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.members()).isEmpty();
        verify(adminMemberDao, never()).findAll(any()); // 목록 조회가 실행되지 않아야 함
    }
}