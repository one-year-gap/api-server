package site.holliverse.admin.application.usecase;

import java.time.Instant;
import java.util.Map;

/**
 * 로그 이벤트.
 */
public record LogFeatureEvent(
        long eventId,
        Instant timestamp,
        String event,
        UserActionFeatureEventName eventName,
        Map<String, Object> eventProperties
) {
}
