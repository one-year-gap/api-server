package site.holliverse.customer.application.usecase.log;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.persistence.entity.UserLogAdminDispatchOutbox;
import site.holliverse.customer.persistence.repository.UserLogAdminDispatchOutboxRepository;
import site.holliverse.shared.monitoring.CustomerMetrics;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@Profile("customer")
@RequiredArgsConstructor
public class UserLogAdminDispatchOutboxStateService {

    private final UserLogAdminDispatchOutboxRepository repository;
    private final CustomerMetrics customerMetrics;

    @Value("${app.userlog.admin-dispatch.retry-delay-ms:10000}")
    private long retryDelayMs;

    @Value("${app.userlog.admin-dispatch.max-attempts:5}")
    private int maxAttempts;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeBatch(List<UserLogAdminDispatchOutbox> rows) {
        try {
            repository.saveAllAndFlush(rows);
            rows.forEach(ignored -> customerMetrics.recordAdminLogFeatureOutbox("stored"));
        } catch (DataIntegrityViolationException e) {
            customerMetrics.recordAdminLogFeatureStoreBatchError("data_integrity");
            throw e;
        } catch (Exception e) {
            customerMetrics.recordAdminLogFeatureStoreBatchError("general");
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void store(UserLogAdminDispatchOutbox row) {
        try {
            repository.saveAndFlush(row);
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

    @Transactional
    public List<UserLogAdminDispatchOutbox> claimReadyBatchRows(int batchSize) {
        List<Long> ids = repository.findReadyEventIdsForUpdate(batchSize);
        if (ids.isEmpty()) {
            return List.of();
        }

        List<UserLogAdminDispatchOutbox> rows = repository.findAllById(ids);
        rows.forEach(UserLogAdminDispatchOutbox::markProcessing);
        repository.saveAll(rows);
        return rows;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAcked(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }

        List<UserLogAdminDispatchOutbox> rows = repository.findAllById(eventIds);
        rows.forEach(row -> {
            row.markAcked();
            customerMetrics.recordAdminLogFeatureOutbox("acked");
        });
        repository.saveAll(rows);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetry(List<Long> eventIds, String errorMessage) {
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }

        List<UserLogAdminDispatchOutbox> rows = repository.findAllById(eventIds);
        rows.forEach(row -> {
            if (row.getAttemptCount() + 1 >= maxAttempts) {
                row.markDead(errorMessage);
                customerMetrics.recordAdminLogFeatureOutbox("dead");
            } else {
                row.markRetry(errorMessage, Instant.now().plusMillis(retryDelayMs));
                customerMetrics.recordAdminLogFeatureOutbox("retry");
            }
        });
        repository.saveAll(rows);
    }
}
