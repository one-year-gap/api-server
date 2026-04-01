package site.holliverse.customer.application.usecase.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.integration.external.AdminLogFeaturesClient;
import site.holliverse.customer.persistence.entity.UserLogAdminDispatchOutbox;
import site.holliverse.customer.persistence.entity.UserLogDispatchStatus;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminLogFeatureDispatchServiceTest {

    @Mock
    private AdminLogFeaturesClient adminLogFeaturesClient;

    @Mock
    private UserLogAdminDispatchOutboxStateService stateService;

    @Test
    @DisplayName("배치 전송 성공 시 eventId 묶음을 ack 처리한다.")
    void dispatchBatch_success_marksAcked() {
        AdminLogFeatureDispatchService service = new AdminLogFeatureDispatchService(adminLogFeaturesClient, stateService);
        List<UserLogAdminDispatchOutbox> rows = List.of(
                outboxRow(11L, 9243L, "click_compare", "2026-04-01T04:36:06.906491988Z"),
                outboxRow(12L, 9243L, "click_penalty", "2026-04-01T04:36:08.906491988Z")
        );
        when(adminLogFeaturesClient.sendLogFeaturesBatch(eq(9243L), anyList()))
                .thenReturn(AdminLogFeaturesClient.DispatchResult.ok());

        service.dispatchBatch(rows);

        ArgumentCaptor<List<AdminLogFeaturesClient.BatchLogEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(adminLogFeaturesClient).sendLogFeaturesBatch(eq(9243L), eventCaptor.capture());
        verify(stateService).markAcked(List.of(11L, 12L));
        verify(stateService, never()).markRetry(anyList(), anyString());
        assertThat(eventCaptor.getValue()).hasSize(2);
        assertThat(eventCaptor.getValue().get(0).eventName()).isEqualTo("click_compare");
    }

    @Test
    @DisplayName("배치 전송 실패 시 eventId 묶음을 retry 처리한다.")
    void dispatchBatch_failure_marksRetry() {
        AdminLogFeatureDispatchService service = new AdminLogFeatureDispatchService(adminLogFeaturesClient, stateService);
        List<UserLogAdminDispatchOutbox> rows = List.of(
                outboxRow(21L, 9243L, "click_compare", "2026-04-01T04:36:06.906491988Z"),
                outboxRow(22L, 9243L, "click_change", "2026-04-01T04:36:07.906491988Z")
        );
        when(adminLogFeaturesClient.sendLogFeaturesBatch(eq(9243L), anyList()))
                .thenReturn(AdminLogFeaturesClient.DispatchResult.fail("non_2xx:503"));

        service.dispatchBatch(rows);

        verify(stateService).markRetry(List.of(21L, 22L), "non_2xx:503");
        verify(stateService, never()).markAcked(anyList());
    }

    private static UserLogAdminDispatchOutbox outboxRow(
            Long eventId,
            Long memberId,
            String eventName,
            String timestamp
    ) {
        return UserLogAdminDispatchOutbox.builder()
                .eventId(eventId)
                .memberId(memberId)
                .eventName(eventName)
                .eventType("click")
                .eventTimestamp(Instant.parse(timestamp))
                .payload(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode())
                .status(UserLogDispatchStatus.PROCESSING)
                .attemptCount(0)
                .build();
    }
}
