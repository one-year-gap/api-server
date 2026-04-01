package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.holliverse.customer.persistence.entity.UserLogAdminDispatchOutbox;

import java.util.List;

@Profile("customer")
public interface UserLogAdminDispatchOutboxRepository extends JpaRepository<UserLogAdminDispatchOutbox, Long> {

    @Query(value = """
            SELECT event_id
            FROM user_log_admin_dispatch_outbox
            WHERE status IN ('READY', 'RETRY')
              AND (next_retry_at IS NULL OR next_retry_at <= now())
            ORDER BY created_at
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findReadyEventIdsForUpdate(@Param("limit") int limit);
}
