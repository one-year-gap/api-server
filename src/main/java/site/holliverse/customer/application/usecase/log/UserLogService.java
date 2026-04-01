package site.holliverse.customer.application.usecase.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

import com.github.f4b6a3.tsid.Tsid;
import site.holliverse.customer.web.dto.log.UserLogRequest;
import site.holliverse.customer.error.CustomerErrorCode;
import site.holliverse.customer.error.CustomerException;
import site.holliverse.shared.monitoring.CustomerMetrics;

import java.util.List;

@Slf4j
@Service
@Profile("customer")
@RequiredArgsConstructor
public class UserLogService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AdminLogFeatureDispatchService adminLogFeatureDispatchService;
    private final CustomerMetrics customerMetrics;

    @Value("${app.topic.client-events}")
    private String topic;

    @Async("userLogTaskExecutor")
    public void publishBatch(Long memberId, List<UserLogRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        customerMetrics.recordUserLogBatchSize(requests.size());
        for (UserLogRequest request : requests) {
            doPublish(memberId, request);
        }

        // 이벤트 전달
        requests.forEach(request -> sendAdminTarget(memberId, request));
    }

    @Async("userLogTaskExecutor")
    public void publish(Long memberId, UserLogRequest request) {
        doPublish(memberId, request);

        // 이벤트 전달
        sendAdminTarget(memberId, request);
    }

    /**
     * 단일 로그를 Kafka로 전송. {@link #publish}, {@link #publishBatch}에서만 호출.
     * self-invocation 시 @Async가 적용되지 않으므로 공통 로직을 private로 분리함.
     */
    private void doPublish(Long memberId, UserLogRequest request) {
        UserLogEventName eventName = UserLogEventName.from(request.eventName());

        long eventId;
        try {
            eventId = decodeTsidToLong(request.tsid());
        } catch (IllegalArgumentException e) {
            customerMetrics.recordUserLogPublish(request.eventName(), "invalid_tsid");
            throw new CustomerException(CustomerErrorCode.INVALID_USER_LOG_EVENT_ID);
        }

        UserLogPayload payload = new UserLogPayload(
                eventId,
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
            customerMetrics.recordUserLogPublish(eventName.value(), "serialization_error");
            log.warn("[UserLog] 직렬화 실패 memberId={}", memberId, e);
            return;
        }

        kafkaTemplate.send(topic, String.valueOf(memberId), json)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        customerMetrics.recordUserLogPublish(eventName.value(), "kafka_error");
                        log.warn("[UserLog] Kafka 전송 실패 memberId={} eventName={}",
                                memberId, eventName.value(), ex);
                        return;
                    }
                    customerMetrics.recordUserLogPublish(eventName.value(), "success");
                });
    }

    /**
     * 프론트에서 전달한 TSID 문자열을 tsid-creator 라이브러리로 디코딩한다.
     */
    private static long decodeTsidToLong(String tsid) {
        if (tsid == null || tsid.isBlank()) {
            throw new IllegalArgumentException("TSID must not be null or blank");
        }
        // Tsid.from(...) 내부에서 형식·길이·알파벳 검증을 수행한다.
        Tsid parsed = Tsid.from(tsid);
        return parsed.toLong();
    }

    /**
     * Admin 이벤트 변환.
     */
    private void sendAdminTarget(Long memberId, UserLogRequest request) {
        UserLogEventName eventName = UserLogEventName.from(request.eventName());
        if (!isAdminTarget(eventName)) {
            return;
        }

        try {
            adminLogFeatureDispatchService.dispatch(
                    memberId,
                    eventName,
                    request.timestamp()
            );
            customerMetrics.recordAdminLogFeatureDispatch("enqueued");
        } catch (TaskRejectedException e) {
            customerMetrics.recordAdminLogFeatureDispatch("rejected");
        }
    }

    /**
     * 대상 이벤트.
     */
    private boolean isAdminTarget(UserLogEventName eventName) {
        return eventName == UserLogEventName.CLICK_COMPARE
                || eventName == UserLogEventName.CLICK_PENALTY
                || eventName == UserLogEventName.CLICK_CHANGE;
    }
}
