package site.holliverse.customer.application.usecase.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.tsid.Tsid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import site.holliverse.customer.persistence.entity.UserLogAdminDispatchOutbox;
import site.holliverse.customer.persistence.entity.UserLogDispatchStatus;
import site.holliverse.customer.web.dto.log.UserLogRequest;
import site.holliverse.shared.monitoring.CustomerMetrics;

import java.util.ArrayList;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile("customer")
@RequiredArgsConstructor
public class UserLogAdminDispatchOutboxService {

    private static final ZoneId BASE_DATE_ZONE = ZoneId.of("Asia/Seoul");

    private final UserLogAdminDispatchOutboxStateService stateService;
    private final AdminLogFeatureDispatchService dispatchService;
    private final ObjectMapper objectMapper;
    private final CustomerMetrics customerMetrics;

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
            stateService.storeBatch(rows);
        } catch (DataIntegrityViolationException e) {
            log.warn("[UserLog][Outbox] batch store fallback. size={}", rows.size(), e);
            rows.forEach(stateService::store);
        }
    }

    public void enqueue(Long eventId, Long memberId, UserLogEventName eventName, UserLogRequest request) {
        stateService.store(buildOutboxRow(eventId, memberId, eventName, request));
    }

    public void dispatchReadyBatch(int batchSize) {
        List<UserLogAdminDispatchOutbox> claimedRows = stateService.claimReadyBatchRows(batchSize);
        for (List<UserLogAdminDispatchOutbox> rows : groupDispatchRows(claimedRows).values()) {
            List<Long> eventIds = rows.stream()
                    .map(UserLogAdminDispatchOutbox::getEventId)
                    .toList();
            try {
                dispatchService.dispatchBatch(rows);
                customerMetrics.recordAdminLogFeatureDispatch("enqueued", "batch");
            } catch (TaskRejectedException e) {
                customerMetrics.recordAdminLogFeatureDispatch("rejected", "batch");
                stateService.markRetry(eventIds, "dispatch_rejected: " + e.getClass().getSimpleName());
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

    private Map<DispatchGroupKey, List<UserLogAdminDispatchOutbox>> groupDispatchRows(
            List<UserLogAdminDispatchOutbox> rows
    ) {
        Map<DispatchGroupKey, List<UserLogAdminDispatchOutbox>> groups = new LinkedHashMap<>();
        for (UserLogAdminDispatchOutbox row : rows) {
            DispatchGroupKey key = new DispatchGroupKey(
                    row.getMemberId(),
                    row.getEventTimestamp().atZone(BASE_DATE_ZONE).toLocalDate()
            );
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(row);
        }
        return groups;
    }

    private record DispatchGroupKey(
            Long memberId,
            LocalDate baseDate
    ) {
    }
}
