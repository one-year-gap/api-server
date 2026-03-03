package site.holliverse.shared.alert.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GrafanaAlertWebhookRequest(
        String receiver,
        String status,
        String externalURL,
        String groupKey,
        Map<String, String> commonLabels,
        Map<String, String> commonAnnotations,
        List<GrafanaAlertItem> alerts
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GrafanaAlertItem(
            String status,
            String fingerprint,
            String generatorURL,
            String dashboardURL,
            String panelURL,
            String silenceURL,
            String startsAt,
            String endsAt,
            Map<String, String> labels,
            Map<String, String> annotations
    ) {
    }
}
