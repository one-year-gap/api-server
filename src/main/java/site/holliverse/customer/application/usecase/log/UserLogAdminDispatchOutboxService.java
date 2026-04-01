package site.holliverse.customer.application.usecase.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.persistence.entity.UserLogAdminDispatchOutbox;
import site.holliverse.customer.persistence.entity.UserLogDispatchStatus;
import site.holliverse.customer.persistence.repository.UserLogAdminDispatchOutboxRepository;
import site.holliverse.customer.web.dto.log.UserLogRequest;
import site.holliverse.shared.monitoring.CustomerMetrics;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@Profile("customer")
@RequiredArgsConstructor
public class UserLogAdminDispatchOutboxService {

    private final UserLogAdminDispatchOutboxRepository repository;
    private final UserLogAdminDispatchOutboxStateService stateService;
    private final AdminLogFeatureDispatchService dispatchService;
    private final ObjectMapper objectMapper;
    private final CustomerMetrics customerMetrics;

    @Transactional
    public void enqueue(Long eventId, Long memberId, UserLogEventName eventName, UserLogRequest request) {
        try {
            UserLogPayload payload = new UserLogPayload(
                    eventId,
                    request.timestamp(),
                    request.event(),
                    eventName.value(),
                    memberId,
                    request.eventProperties()
            );

            repository.saveAndFlush(UserLogAdminDispatchOutbox.builder()
                    .eventId(eventId)
                    .memberId(memberId)
                    .eventName(eventName.value())
                    .eventType(request.event())
                    .eventTimestamp(Instant.parse(request.timestamp()))
                    .payload(objectMapper.valueToTree(payload))
                    .status(UserLogDispatchStatus.READY)
                    .attemptCount(0)
                    .build());
            customerMetrics.recordAdminLogFeatureOutbox("stored");
        } catch (DataIntegrityViolationException e) {
            customerMetrics.recordAdminLogFeatureOutbox("duplicate");
        } catch (Exception e) {
            customerMetrics.recordAdminLogFeatureOutbox("store_error");
        }
    }

    public void dispatchReadyBatch(int batchSize) {
        List<Long> claimedIds = stateService.claimReadyBatch(batchSize);
        for (Long eventId : claimedIds) {
            try {
                dispatchService.dispatch(eventId);
                customerMetrics.recordAdminLogFeatureDispatch("enqueued");
            } catch (TaskRejectedException e) {
                customerMetrics.recordAdminLogFeatureDispatch("rejected");
                stateService.markRetry(eventId, "dispatch_rejected: " + e.getClass().getSimpleName());
            }
        }
    }
}
