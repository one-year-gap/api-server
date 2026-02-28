package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.query.dao.MemberDetailRawData;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMemberDetailUseCaseTest {

    @Mock
    private AdminMemberDao adminMemberDao;
    @InjectMocks
    private GetMemberDetailUseCase useCase;

    @Test
    @DisplayName("회원 상세 조회 성공 - 정상적으로 데이터를 반환한다")
    void execute_success() {
        // given
        Long memberId = 1L;
        MemberDetailRawData mockRaw = new MemberDetailRawData(
                "enc_name", "enc_phone", "test@test.com", LocalDate.of(1995, 1, 1),
                "M", "VIP", LocalDate.now(), "ACTIVE", "Province", "City", "Street", "5G 요금제",
                LocalDateTime.now().minusMonths(12), // contractStartDate (1년 전 가입)
                24,                                  // contractMonths (24개월 약정)
                LocalDateTime.now().plusMonths(12),  // contractEndDate (1년 뒤 만료)
                5L,                                  // totalSupportCount (총 상담 5번)
                LocalDateTime.now().minusDays(3)     // lastSupportDate (3일 전 마지막 상담)
        );
        given(adminMemberDao.findDetailById(memberId)).willReturn(Optional.of(mockRaw));

        // when
        MemberDetailRawData result = useCase.execute(memberId);

        // then
        assertThat(result.email()).isEqualTo("test@test.com");
        assertThat(result.contractMonths()).isEqualTo(24);
        assertThat(result.totalSupportCount()).isEqualTo(5L);
        verify(adminMemberDao, times(1)).findDetailById(memberId);
    }

    @Test
    @DisplayName("회원 상세 조회 실패 - 존재하지 않는 ID인 경우 CustomException이 발생한다")
    void execute_fail_notFound() {
        // given
        Long memberId = 999L;
        given(adminMemberDao.findDetailById(memberId)).willReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> useCase.execute(memberId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(exception.getReason()).contains("999");
    }
}