package site.holliverse.shared.alert.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.holliverse.shared.alert.model.AlertIncidentStatus;
import site.holliverse.shared.persistence.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "alert_incident")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlertIncident extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_id")
    private Long id;

    @Column(name = "fingerprint", nullable = false, unique = true, length = 200)
    private String fingerprint;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AlertIncidentStatus status;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "owner", nullable = false, length = 100)
    private String owner;

    @Column(name = "team", nullable = false, length = 100)
    private String team;

    @Column(name = "service_name", length = 100)
    private String service;

    @Column(name = "service_version", length = 100)
    private String version;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "http_method", length = 10)
    private String method;

    @Column(name = "uri_template", length = 255)
    private String uriTemplate;

    @Column(name = "alert_name", length = 255)
    private String alertName;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "grafana_alert_url", columnDefinition = "TEXT")
    private String grafanaAlertUrl;

    @Column(name = "runbook_url", columnDefinition = "TEXT")
    private String runbookUrl;

    @Column(name = "first_seen_at", nullable = false)
    private LocalDateTime firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "event_count", nullable = false)
    private int eventCount;

    @Column(name = "last_notified_at")
    private LocalDateTime lastNotifiedAt;

    @Column(name = "ack_by", length = 100)
    private String ackBy;

    @Column(name = "ack_comment", columnDefinition = "TEXT")
    private String ackComment;

    @Column(name = "ack_at")
    private LocalDateTime ackAt;

    public static AlertIncident createNew(String fingerprint,
                                          String severity,
                                          String owner,
                                          String team,
                                          String service,
                                          String version,
                                          String errorCode,
                                          String method,
                                          String uriTemplate,
                                          String alertName,
                                          String summary,
                                          String message,
                                          String grafanaAlertUrl,
                                          String runbookUrl,
                                          LocalDateTime now) {
        AlertIncident incident = new AlertIncident();
        incident.fingerprint = fingerprint;
        incident.status = AlertIncidentStatus.OPEN;
        incident.severity = severity;
        incident.owner = owner;
        incident.team = team;
        incident.service = service;
        incident.version = version;
        incident.errorCode = errorCode;
        incident.method = method;
        incident.uriTemplate = uriTemplate;
        incident.alertName = alertName;
        incident.summary = summary;
        incident.message = message;
        incident.grafanaAlertUrl = grafanaAlertUrl;
        incident.runbookUrl = runbookUrl;
        incident.firstSeenAt = now;
        incident.lastSeenAt = now;
        incident.eventCount = 1;
        return incident;
    }

    public void markFiredAgain(String severity,
                               String summary,
                               String message,
                               String grafanaAlertUrl,
                               String runbookUrl,
                               LocalDateTime now) {
        this.status = AlertIncidentStatus.OPEN;
        this.severity = severity;
        this.summary = summary;
        this.message = message;
        this.grafanaAlertUrl = grafanaAlertUrl;
        if (runbookUrl != null && !runbookUrl.isBlank()) {
            this.runbookUrl = runbookUrl;
        }
        this.lastSeenAt = now;
        this.eventCount = this.eventCount + 1;
    }

    public void acknowledge(String actor, String comment, LocalDateTime now) {
        this.status = AlertIncidentStatus.ACKED;
        this.ackBy = actor;
        this.ackComment = comment;
        this.ackAt = now;
    }

    public void updateRunbook(String runbookUrl) {
        if (runbookUrl != null && !runbookUrl.isBlank()) {
            this.runbookUrl = runbookUrl;
        }
    }

    public boolean canNotify(LocalDateTime now, int cooldownSeconds) {
        if (lastNotifiedAt == null) {
            return true;
        }
        return lastNotifiedAt.plusSeconds(cooldownSeconds).isBefore(now);
    }

    public void markNotified(LocalDateTime now) {
        this.lastNotifiedAt = now;
    }
}
