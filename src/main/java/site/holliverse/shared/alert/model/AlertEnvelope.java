package site.holliverse.shared.alert.model;

public record AlertEnvelope(
        String fingerprint,
        String status,
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
        String runbookUrl
) {
}
