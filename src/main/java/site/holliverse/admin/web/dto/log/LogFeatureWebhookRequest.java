package site.holliverse.admin.web.dto.log;

public record LogFeatureWebhookRequest(
        String eventType,
        Long memberId,
        String timeStamp
) {
}
