package site.holliverse.customer.application.usecase.log;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.persistence.entity.UserLogAdminDispatchOutbox;
import site.holliverse.customer.persistence.repository.UserLogAdminDispatchOutboxRepository;
import site.holliverse.shared.monitoring.CustomerMetrics;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

    @Transactional
    public List<Long> claimReadyBatch(int batchSize) {
        List<Long> ids = repository.findReadyEventIdsForUpdate(batchSize);
        if (ids.isEmpty()) {
            return ids;
        }

        List<UserLogAdminDispatchOutbox> rows = repository.findAllById(ids);
        rows.forEach(UserLogAdminDispatchOutbox::markProcessing);
        repository.saveAll(rows);
        return ids;
    }

    @Transactional(readOnly = true)
    public Optional<UserLogAdminDispatchOutbox> get(Long eventId) {
        return repository.findById(eventId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAcked(Long eventId) {
        repository.findById(eventId).ifPresent(row -> {
            row.markAcked();
            repository.save(row);
            customerMetrics.recordAdminLogFeatureOutbox("acked");
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetry(Long eventId, String errorMessage) {
        repository.findById(eventId).ifPresent(row -> {
            if (row.getAttemptCount() + 1 >= maxAttempts) {
                row.markDead(errorMessage);
                customerMetrics.recordAdminLogFeatureOutbox("dead");
            } else {
                row.markRetry(errorMessage, Instant.now().plusMillis(retryDelayMs));
                customerMetrics.recordAdminLogFeatureOutbox("retry");
            }
            repository.save(row);
        });
    }
}
