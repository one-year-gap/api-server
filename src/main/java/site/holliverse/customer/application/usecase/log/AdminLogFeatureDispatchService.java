package site.holliverse.customer.application.usecase.log;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.holliverse.customer.integration.external.AdminLogFeaturesClient;
import site.holliverse.customer.persistence.entity.UserLogAdminDispatchOutbox;

import java.util.List;

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
    public void dispatchBatch(List<UserLogAdminDispatchOutbox> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        List<Long> eventIds = rows.stream()
                .map(UserLogAdminDispatchOutbox::getEventId)
                .toList();

        try {
            var result = adminLogFeaturesClient.sendLogFeaturesBatch(
                    rows.get(0).getMemberId(),
                    rows.stream()
                            .map(row -> new AdminLogFeaturesClient.BatchLogEvent(
                                    row.getEventId(),
                                    row.getEventTimestamp().toString(),
                                    row.getEventType(),
                                    row.getEventName(),
                                    row.getPayload().path("event_properties")
                            ))
                            .toList()
            );

            if (result.success()) {
                stateService.markAcked(eventIds);
                return;
            }

            stateService.markRetry(eventIds, result.errorMessage());
        } catch (Exception e) {
            stateService.markRetry(eventIds, e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }
}
