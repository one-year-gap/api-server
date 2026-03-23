package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.error.AdminErrorCode;
import site.holliverse.admin.error.AdminException;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.query.dao.MemberDetailRawData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMemberDetailUseCaseTest {

    @Mock
    private AdminMemberDao adminMemberDao;
    @InjectMocks
    private GetMemberDetailUseCase useCase;

    @Test
    @DisplayName("회원 상세 조회 성공 - 정상적으로 데이터와 Top3 키워드를 반환한다")
    void execute_success() {
        // given
        Long memberId = 1L;

        // 1. RawData 가짜 객체 생성
        MemberDetailRawData mockRaw = new MemberDetailRawData(
                "enc_name", "enc_phone", "test@test.com", LocalDate.of(1995, 1, 1),
                "M", "VIP", LocalDate.now(), "ACTIVE", "Province", "City", "Street", "5G 요금제",
                LocalDateTime.now().minusMonths(12), // contractStartDate (1년 전 가입)
                24,                                  // contractMonths (24개월 약정)
                LocalDateTime.now().plusMonths(12),  // contractEndDate (1년 뒤 만료)
                5L,                                  // totalSupportCount (총 상담 5번)
                LocalDateTime.now().minusDays(3),    // lastSupportDate (3일 전 마지막 상담)
                "CLOSED",                            // recentSupportStatus
                5,                                   // recentSatisfactionScore
                4.5                                  // averageSatisfactionScore
        );

        // 2. 키워드 Top3 가짜 데이터 생성
        List<String> mockKeywords = List.of("가입/해지", "요금제", "단말기 파손");

        // 3. DAO 동작 Mocking
        given(adminMemberDao.findDetailById(memberId)).willReturn(Optional.of(mockRaw));
        given(adminMemberDao.findTop3KeywordsByMemberId(memberId)).willReturn(mockKeywords);

        // when
        GetMemberDetailUseCase.GetMemberDetailResult result = useCase.execute(memberId);

        // then
        // 4. 기존 RawData 내용물 검증
        assertThat(result.rawData().email()).isEqualTo("test@test.com");
        assertThat(result.rawData().contractMonths()).isEqualTo(24);
        assertThat(result.rawData().totalSupportCount()).isEqualTo(5L);
        assertThat(result.rawData().recentSupportStatus()).isEqualTo("CLOSED");

        // 5. 키워드 리스트 검증
        assertThat(result.top3Keywords())
                .hasSize(3)
                .containsExactly("가입/해지", "요금제", "단말기 파손");

        // 6. DAO 메서드들이 정확히 1번씩 호출되었는지 검증
        verify(adminMemberDao, times(1)).findDetailById(memberId);
        verify(adminMemberDao, times(1)).findTop3KeywordsByMemberId(memberId);
    }

    @Test
    @DisplayName("회원 상세 조회 실패 - 존재하지 않는 ID인 경우 AdminException이 발생한다")
    void execute_fail_notFound() {
        // given
        Long memberId = 999L;
        given(adminMemberDao.findDetailById(memberId)).willReturn(Optional.empty());

        // when & then
        AdminException exception = assertThrows(AdminException.class, () -> useCase.execute(memberId));
        assertThat(exception.getErrorCode()).isEqualTo(AdminErrorCode.MEMBER_NOT_FOUND);

        // 회원이 없어서 예외가 터졌으므로, 두 번째 쿼리(키워드 조회)는 절대 실행되지 않아야 함
        verify(adminMemberDao, never()).findTop3KeywordsByMemberId(anyLong());
    }
}
