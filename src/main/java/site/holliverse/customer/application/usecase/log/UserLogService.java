package site.holliverse.customer.application.usecase.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import site.holliverse.customer.web.dto.log.UserLogRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@Profile("customer")
@RequiredArgsConstructor
public class UserLogService {

    private static final Set<String> ALLOWED_EVENT_NAMES = Set.of(
            "click_product_detail",
            "click_list_type",
            "click_compare",
            "click_change",
            "click_change_success",
            "click_penalty",
            "click_coupon"
    );

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.topic.client-events}")    
    private String topic;
    public void publish(Long memberId, UserLogRequest request) {
        if (!ALLOWED_EVENT_NAMES.contains(request.eventName())) {
            throw new IllegalArgumentException("허용되지 않는 event_name: " + request.getEventName());
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event_id", request.eventId());
        payload.put("timestamp", request.timestamp());
        payload.put("event", request.event());
        payload.put("event_name", request.eventName());
        payload.put("member_id", memberId);
        payload.put("event_properties", request.eventProperties());

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("[UserLog] 직렬화 실패 memberId={}", memberId, e);
            return;
        }

        kafkaTemplate.send(topic, String.valueOf(memberId), json)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("[UserLog] Kafka 전송 실패 memberId={} eventName={}",
                                memberId, request.eventName(), ex);
                    }
                });
    }
}

