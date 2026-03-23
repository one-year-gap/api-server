package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.error.AdminErrorCode;
import site.holliverse.admin.error.AdminException;
import site.holliverse.admin.query.dao.AdminChurnCouponDao;
import site.holliverse.admin.web.dto.churn.IssueChurnCouponRequestDto;
import site.holliverse.admin.web.dto.churn.IssueChurnCouponResponseDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IssueChurnCouponUseCaseTest {

    @Mock
    private AdminChurnCouponDao adminChurnCouponDao;

    @Mock
    private ChurnCouponIssueProcessor churnCouponIssueProcessor;

    @Mock
    private ChurnCouponSmsService churnCouponSmsService;

    @InjectMocks
    private IssueChurnCouponUseCase issueChurnCouponUseCase;

    @Test
    @DisplayName("존재하지 않는 쿠폰 ID로 요청하면 NOT_FOUND 예외가 발생한다.")
    void execute_fail_couponNotFound() {
        IssueChurnCouponRequestDto requestDto = new IssueChurnCouponRequestDto(
                List.of(1L, 3L),
                999L
        );

        given(adminChurnCouponDao.existsCouponById(999L)).willReturn(false);

        AdminException exception = assertThrows(AdminException.class,
                () -> issueChurnCouponUseCase.execute(requestDto));

        assertThat(exception.getErrorCode()).isEqualTo(AdminErrorCode.COUPON_NOT_FOUND);
        verify(churnCouponIssueProcessor, never()).issue(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("쿠폰 발송 시 회원별 결과를 집계해 응답한다.")
    void execute_success_withPartialResult() {
        IssueChurnCouponRequestDto requestDto = new IssueChurnCouponRequestDto(
                List.of(1L, 3L),
                1L
        );

        given(adminChurnCouponDao.existsCouponById(1L)).willReturn(true);
        given(churnCouponIssueProcessor.issue(1L, 1L))
                .willReturn(IssueOneChurnCouponResult.issued(1L));
        given(churnCouponIssueProcessor.issue(3L, 1L))
                .willReturn(IssueOneChurnCouponResult.skipped(3L, "ALREADY_ISSUED_WITHIN_90_DAYS"));

        IssueChurnCouponResponseDto result = issueChurnCouponUseCase.execute(requestDto);

        assertThat(result.requestedCount()).isEqualTo(2);
        assertThat(result.issuedCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(1);
        assertThat(result.issuedMemberIds()).containsExactly(1L);
        assertThat(result.skippedMembers()).hasSize(1);
        assertThat(result.skippedMembers().get(0).memberId()).isEqualTo(3L);
        assertThat(result.skippedMembers().get(0).reason()).isEqualTo("ALREADY_ISSUED_WITHIN_90_DAYS");
    }
}
