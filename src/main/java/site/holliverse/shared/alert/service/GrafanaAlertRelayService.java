package site.holliverse.shared.alert.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import site.holliverse.shared.alert.config.AlertProperties;
import site.holliverse.shared.alert.model.AlertEnvelope;
import site.holliverse.shared.alert.persistence.entity.AlertIncident;
import site.holliverse.shared.alert.web.dto.GrafanaAlertWebhookRequest;
import site.holliverse.shared.logging.LogStaticContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GrafanaAlertRelayService {

    private static final String STATUS_FIRING = "firing";

    private final AlertProperties alertProperties;
    private final AlertIncidentService alertIncidentService;
    private final DiscordBotClient discordBotClient;
    private final LogStaticContext logStaticContext;

    public GrafanaAlertRelayService(AlertProperties alertProperties,
                                    AlertIncidentService alertIncidentService,
                                    DiscordBotClient discordBotClient,
                                    LogStaticContext logStaticContext) {
        this.alertProperties = alertProperties;
        this.alertIncidentService = alertIncidentService;
        this.discordBotClient = discordBotClient;
        this.logStaticContext = logStaticContext;
    }

    public void handleWebhook(GrafanaAlertWebhookRequest request) {
        if (!alertProperties.isRelayEnabled()) {
            return;
        }

        List<GrafanaAlertWebhookRequest.GrafanaAlertItem> alerts = request.alerts();
        if (alerts == null || alerts.isEmpty()) {
            return;
        }

        for (GrafanaAlertWebhookRequest.GrafanaAlertItem item : alerts) {
            AlertEnvelope envelope = toEnvelope(request, item);
            String normalizedStatus = normalizeStatus(item.status(), request.status());

            if (!STATUS_FIRING.equals(normalizedStatus)) {
                continue;
            }

            AlertIncident incident = alertIncidentService.recordFiring(envelope);
            notifyOwnerIfNeeded(incident, envelope);
        }
    }

    private void notifyOwnerIfNeeded(AlertIncident incident, AlertEnvelope envelope) {
        AlertProperties.OwnerConfig ownerConfig = alertProperties.getOwners().get(envelope.owner());
        if (ownerConfig == null || !StringUtils.hasText(ownerConfig.getDiscordUserId())) {
            return;
        }
        if (!incident.canNotify(LocalDateTime.now(), alertProperties.getNotifyCooldownSeconds())) {
            return;
        }

        String message = buildDirectMessage(incident, envelope);
        discordBotClient.sendDirectMessage(ownerConfig.getDiscordUserId(), message);
        alertIncidentService.markNotified(incident.getId(), LocalDateTime.now());
    }

    private AlertEnvelope toEnvelope(GrafanaAlertWebhookRequest request, GrafanaAlertWebhookRequest.GrafanaAlertItem item) {
        Map<String, String> labels = mergeStringMap(request.commonLabels(), item.labels());
        Map<String, String> annotations = mergeStringMap(request.commonAnnotations(), item.annotations());

        String owner = firstNonBlank(labels.get("owner"), alertProperties.getDefaultOwner());
        AlertProperties.OwnerConfig ownerConfig = alertProperties.getOwners().get(owner);

        String runbookUrl = firstNonBlank(
                annotations.get("runbook_url"),
                ownerConfig != null ? ownerConfig.getRunbookUrl() : null,
                alertProperties.getDefaultRunbookUrl()
        );
        String method = firstNonBlank(labels.get("method"), labels.get("http_method"), "-");
        String uriTemplate = firstNonBlank(labels.get("uriTemplate"), labels.get("uri_template"), "-");
        String errorCode = firstNonBlank(labels.get("errorCode"), labels.get("error_code"), "UNKNOWN");
        String alertName = firstNonBlank(labels.get("alertname"), "api-error");
        String service = firstNonBlank(labels.get("service"), logStaticContext.service());
        String version = firstNonBlank(labels.get("version"), logStaticContext.version());
        String team = firstNonBlank(
                labels.get("team"),
                ownerConfig != null ? ownerConfig.getTeam() : null,
                logStaticContext.team()
        );
        String severity = firstNonBlank(labels.get("severity"), "normal");
        String summary = firstNonBlank(annotations.get("summary"), annotations.get("description"), "alert fired");
        String message = firstNonBlank(annotations.get("message"), annotations.get("description"), summary);
        String grafanaAlertUrl = firstNonBlank(item.panelURL(), item.generatorURL(), request.externalURL());

        String fingerprint = firstNonBlank(
                item.fingerprint(),
                sha256(owner + "|" + method + "|" + uriTemplate + "|" + errorCode + "|" + alertName)
        );

        return new AlertEnvelope(
                fingerprint,
                normalizeStatus(item.status(), request.status()),
                severity,
                owner,
                team,
                service,
                version,
                errorCode,
                method,
                uriTemplate,
                alertName,
                summary,
                message,
                grafanaAlertUrl,
                runbookUrl
        );
    }

    private String buildDirectMessage(AlertIncident incident, AlertEnvelope envelope) {
        StringBuilder builder = new StringBuilder();
        builder.append("ALERT Assigned: `").append(envelope.owner()).append("`\n")
                .append("- incidentId: `").append(incident.getId()).append("`\n")
                .append("- severity: `").append(envelope.severity()).append("`\n")
                .append("- errorCode: `").append(envelope.errorCode()).append("`\n")
                .append("- method: `").append(envelope.method()).append("`\n")
                .append("- uriTemplate: `").append(envelope.uriTemplate()).append("`\n")
                .append("- message: `").append(trim(envelope.message())).append("`\n");

        if (StringUtils.hasText(envelope.grafanaAlertUrl())) {
            builder.append("- grafana: ").append(envelope.grafanaAlertUrl()).append("\n");
        }
        if (StringUtils.hasText(envelope.runbookUrl())) {
            builder.append("- runbook: ").append(envelope.runbookUrl()).append("\n");
        }

        builder.append("- ack-api: `POST /internal/alerts/incidents/")
                .append(incident.getId())
                .append("/ack`");
        return builder.toString();
    }

    private String trim(String message) {
        if (message == null || message.isBlank()) {
            return "-";
        }
        if (message.length() <= 220) {
            return message;
        }
        return message.substring(0, 220) + "...";
    }

    private String normalizeStatus(String itemStatus, String requestStatus) {
        String value = firstNonBlank(itemStatus, requestStatus, STATUS_FIRING);
        return value.trim().toLowerCase();
    }

    private Map<String, String> mergeStringMap(Map<String, String> base, Map<String, String> override) {
        Map<String, String> result = new HashMap<>();
        if (base != null) {
            result.putAll(base);
        }
        if (override != null) {
            result.putAll(override);
        }
        return result;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
