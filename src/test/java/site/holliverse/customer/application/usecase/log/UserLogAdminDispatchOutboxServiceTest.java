package site.holliverse.customer.application.usecase.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.persistence.entity.UserLogAdminDispatchOutbox;
import site.holliverse.customer.persistence.entity.UserLogDispatchStatus;
import site.holliverse.shared.monitoring.CustomerMetrics;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserLogAdminDispatchOutboxServiceTest {

    @Mock
    private UserLogAdminDispatchOutboxStateService stateService;

    @Mock
    private AdminLogFeatureDispatchService dispatchService;

    @Mock
    private CustomerMetrics customerMetrics;

    @Test
    @DisplayName("claim된 outbox는 memberId와 KST 기준일 단위로 묶어서 배치 전송한다.")
    void dispatchReadyBatch_groupsByMemberIdAndBaseDate() {
        UserLogAdminDispatchOutboxService service = new UserLogAdminDispatchOutboxService(
                stateService,
                dispatchService,
                new ObjectMapper(),
                customerMetrics
        );
        List<UserLogAdminDispatchOutbox> rows = List.of(
                outboxRow(1L, 9243L, "2026-04-01T04:36:06.906491988Z"),
                outboxRow(2L, 9243L, "2026-04-01T14:36:06.906491988Z"),
                outboxRow(3L, 9243L, "2026-04-01T15:10:00.000000000Z")
        );
        when(stateService.claimReadyBatchRows(20)).thenReturn(rows);

        service.dispatchReadyBatch(20);

        ArgumentCaptor<List<UserLogAdminDispatchOutbox>> batchCaptor = ArgumentCaptor.forClass(List.class);
        verify(dispatchService, times(2)).dispatchBatch(batchCaptor.capture());
        verify(customerMetrics, times(2)).recordAdminLogFeatureDispatch("enqueued", "batch");

        List<List<UserLogAdminDispatchOutbox>> captured = batchCaptor.getAllValues();
        assertThat(captured).hasSize(2);
        assertThat(captured.stream().map(List::size)).containsExactlyInAnyOrder(2, 1);
        assertThat(captured.stream()
                .filter(batch -> batch.size() == 2)
                .findFirst()
                .orElseThrow())
                .extracting(UserLogAdminDispatchOutbox::getEventId)
                .containsExactly(1L, 2L);
    }

    private static UserLogAdminDispatchOutbox outboxRow(Long eventId, Long memberId, String timestamp) {
        return UserLogAdminDispatchOutbox.builder()
                .eventId(eventId)
                .memberId(memberId)
                .eventName("click_compare")
                .eventType("click")
                .eventTimestamp(Instant.parse(timestamp))
                .payload(new ObjectMapper().createObjectNode())
                .status(UserLogDispatchStatus.READY)
                .attemptCount(0)
                .build();
    }
}
