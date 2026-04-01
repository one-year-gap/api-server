package site.holliverse.customer.application.usecase.log;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.holliverse.customer.integration.external.AdminLogFeaturesClient;
import site.holliverse.customer.persistence.entity.UserLogAdminDispatchOutbox;

/**
 * admin log-feature 별도 executor로 분리
 */
@Slf4j
@Service
@Profile("customer")
@RequiredArgsConstructor
public class AdminLogFeatureDispatchService {

    private final AdminLogFeaturesClient adminLogFeaturesClient;
    private final UserLogAdminDispatchOutboxStateService stateService;

    @Async("adminLogFeatureTaskExecutor")
    public void dispatch(Long eventId) {
        stateService.get(eventId).ifPresentOrElse(
                this::dispatchClaimedRow,
                () -> log.warn("[UserLog][Outbox] claimed row not found eventId={}", eventId)
        );
    }

    private void dispatchClaimedRow(UserLogAdminDispatchOutbox row) {
        try {
            var result = adminLogFeaturesClient.sendLogFeature(
                    row.getMemberId(),
                    UserLogEventName.from(row.getEventName()),
                    row.getEventTimestamp().toString()
            );

            if (result.success()) {
                stateService.markAcked(row.getEventId());
                return;
            }

            stateService.markRetry(row.getEventId(), result.errorMessage());
        } catch (Exception e) {
            stateService.markRetry(row.getEventId(), e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }
}
