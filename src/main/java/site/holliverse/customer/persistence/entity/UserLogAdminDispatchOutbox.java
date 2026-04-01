package site.holliverse.customer.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import site.holliverse.shared.persistence.BaseEntity;

import java.time.Instant;

@Entity
@Table(name = "user_log_admin_dispatch_outbox")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLogAdminDispatchOutbox extends BaseEntity {

    @Id
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "event_name", nullable = false, length = 200)
    private String eventName;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserLogDispatchStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public void markProcessing() {
        this.status = UserLogDispatchStatus.PROCESSING;
        this.lastError = null;
    }

    public void markAcked() {
        this.status = UserLogDispatchStatus.ACKED;
        this.nextRetryAt = null;
        this.lastError = null;
    }

    public void markRetry(String errorMessage, Instant nextRetryAt) {
        this.status = UserLogDispatchStatus.RETRY;
        this.attemptCount += 1;
        this.nextRetryAt = nextRetryAt;
        this.lastError = errorMessage;
    }

    public void markDead(String errorMessage) {
        this.status = UserLogDispatchStatus.DEAD;
        this.attemptCount += 1;
        this.nextRetryAt = null;
        this.lastError = errorMessage;
    }
}
