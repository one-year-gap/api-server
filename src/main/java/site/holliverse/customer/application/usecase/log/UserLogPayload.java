package site.holliverse.customer.application.usecase.log;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Kafka로 전송되는 사용자 로그 페이로드.
 */
public record UserLogPayload(
        @JsonProperty("event_id")
        Long eventId,
        String timestamp,
        String event,
        @JsonProperty("event_name")
        String eventName,
        @JsonProperty("member_id")
        Long memberId,
        @JsonProperty("event_properties")
        Map<String, Object> eventProperties
) {
}

