package site.holliverse.customer.application.usecase.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import site.holliverse.customer.integration.external.AdminLogFeaturesClient;
import site.holliverse.customer.web.dto.log.UserLogRequest;

import java.util.List;

@Slf4j
@Service
@Profile("customer")
@RequiredArgsConstructor
public class UserLogService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AdminLogFeaturesClient adminLogFeaturesClient;

    @Value("${app.topic.client-events}")
    private String topic;

    @Async("userLogTaskExecutor")
    public void publishBatch(Long memberId, List<UserLogRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        for (UserLogRequest request : requests) {
            doPublish(memberId, request);
        }
        // event_name 기준 배치 내 중복 제거 후 Admin log-features 호출 (comparison/penalty 각 최대 1)
        int comparisonIncrement = requests.stream()
                .anyMatch(r -> UserLogEventName.CLICK_COMPARE.value().equals(r.eventName())) ? 1 : 0;
        int penaltyIncrement = requests.stream()
                .anyMatch(r -> UserLogEventName.CLICK_PENALTY.value().equals(r.eventName())) ? 1 : 0;
        if (comparisonIncrement != 0 || penaltyIncrement != 0) {
            adminLogFeaturesClient.sendLogFeatures(memberId, comparisonIncrement, penaltyIncrement);
        }
    }

    @Async("userLogTaskExecutor")
    public void publish(Long memberId, UserLogRequest request) {
        doPublish(memberId, request);
    }

    /**
     * 단일 로그를 Kafka로 전송. {@link #publish}, {@link #publishBatch}에서만 호출.
     * self-invocation 시 @Async가 적용되지 않으므로 공통 로직을 private로 분리함.
     */
    private void doPublish(Long memberId, UserLogRequest request) {
        UserLogEventName eventName = UserLogEventName.from(request.eventName());

        UserLogPayload payload = new UserLogPayload(
                request.eventId(),
                request.timestamp(),
                request.event(),
                eventName.value(),
                memberId,
                request.eventProperties()
        );

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
                                memberId, eventName.value(), ex);
                    }
                });
    }
}

