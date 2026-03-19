package site.holliverse.admin.web.dto.log;

import site.holliverse.customer.application.usecase.log.UserLogEventName;

public record LogFeatureWebhookRequest(
        UserLogEventName eventType,
        Long memberId,
        String timeStamp
) {
}
