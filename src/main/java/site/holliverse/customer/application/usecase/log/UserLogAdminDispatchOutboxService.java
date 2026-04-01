package site.holliverse.customer.application.usecase.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.tsid.Tsid;
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

import java.util.ArrayList;
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
    public void enqueueBatch(Long memberId, List<UserLogRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<UserLogAdminDispatchOutbox> rows = new ArrayList<>();
        for (UserLogRequest request : requests) {
            UserLogEventName eventName = UserLogEventName.from(request.eventName());
            if (!isAdminTarget(eventName)) {
                continue;
            }

            try {
                rows.add(buildOutboxRow(decodeTsidToLong(request.tsid()), memberId, eventName, request));
            } catch (IllegalArgumentException e) {
                customerMetrics.recordAdminLogFeatureOutbox("invalid_tsid");
                log.warn("[UserLog][Outbox] invalid tsid. memberId={}, eventName={}, tsid={}",
                        memberId, eventName.value(), request.tsid(), e);
            } catch (Exception e) {
                customerMetrics.recordAdminLogFeatureOutbox("store_error");
                log.warn("[UserLog][Outbox] build failed memberId={} eventName={}",
                        memberId, eventName.value(), e);
            }
        }

        if (rows.isEmpty()) {
            return;
        }

        try {
            repository.saveAll(rows);
            rows.forEach(ignored -> customerMetrics.recordAdminLogFeatureOutbox("stored"));
        } catch (DataIntegrityViolationException e) {
            log.warn("[UserLog][Outbox] batch store fallback. size={}", rows.size(), e);
            rows.forEach(this::storeRow);
        }
    }

    @Transactional
    public void enqueue(Long eventId, Long memberId, UserLogEventName eventName, UserLogRequest request) {
        storeRow(buildOutboxRow(eventId, memberId, eventName, request));
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

    private UserLogAdminDispatchOutbox buildOutboxRow(
            Long eventId,
            Long memberId,
            UserLogEventName eventName,
            UserLogRequest request
    ) {
        UserLogPayload payload = new UserLogPayload(
                eventId,
                request.timestamp(),
                request.event(),
                eventName.value(),
                memberId,
                request.eventProperties()
        );

        return UserLogAdminDispatchOutbox.builder()
                .eventId(eventId)
                .memberId(memberId)
                .eventName(eventName.value())
                .eventType(request.event())
                .eventTimestamp(Instant.parse(request.timestamp()))
                .payload(objectMapper.valueToTree(payload))
                .status(UserLogDispatchStatus.READY)
                .attemptCount(0)
                .build();
    }

    private void storeRow(UserLogAdminDispatchOutbox row) {
        try {
            repository.save(row);
            customerMetrics.recordAdminLogFeatureOutbox("stored");
        } catch (DataIntegrityViolationException e) {
            customerMetrics.recordAdminLogFeatureOutbox("duplicate");
            log.debug("[UserLog][Outbox] duplicate event_id={} memberId={} eventName={}",
                    row.getEventId(), row.getMemberId(), row.getEventName());
        } catch (Exception e) {
            customerMetrics.recordAdminLogFeatureOutbox("store_error");
            log.warn("[UserLog][Outbox] store failed event_id={} memberId={} eventName={}",
                    row.getEventId(), row.getMemberId(), row.getEventName(), e);
        }
    }

    private boolean isAdminTarget(UserLogEventName eventName) {
        return eventName == UserLogEventName.CLICK_COMPARE
                || eventName == UserLogEventName.CLICK_PENALTY
                || eventName == UserLogEventName.CLICK_CHANGE;
    }

    private static long decodeTsidToLong(String tsid) {
        if (tsid == null || tsid.isBlank()) {
            throw new IllegalArgumentException("TSID must not be null or blank");
        }
        return Tsid.from(tsid).toLong();
    }
}
