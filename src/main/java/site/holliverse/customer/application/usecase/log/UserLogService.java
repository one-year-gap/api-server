package site.holliverse.customer.application.usecase.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.tsid.Tsid;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.holliverse.customer.error.CustomerErrorCode;
import site.holliverse.customer.error.CustomerException;
import site.holliverse.customer.integration.external.AdminLogFeaturesClient;
import site.holliverse.customer.web.dto.log.UserLogRequest;
import site.holliverse.shared.monitoring.CustomerMetrics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Profile("customer")
public class UserLogService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AdminLogFeaturesClient adminLogFeaturesClient;
    private final CustomerMetrics customerMetrics;
    private final MeterRegistry meterRegistry;
    private final DistributionSummary batchSizeSummary;
    private final Map<String, Counter> requestCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> resultCounters = new ConcurrentHashMap<>();

    public UserLogService(KafkaTemplate<String, String> kafkaTemplate,
                          ObjectMapper objectMapper,
                          AdminLogFeaturesClient adminLogFeaturesClient,
                          CustomerMetrics customerMetrics,
                          MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.adminLogFeaturesClient = adminLogFeaturesClient;
        this.customerMetrics = customerMetrics;
        this.meterRegistry = meterRegistry;
        this.batchSizeSummary = DistributionSummary.builder("holliverse.userlog.batch.size")
                .description("Batch size of user log submissions")
                .register(meterRegistry);
    }

    @Value("${app.topic.client-events}")
    private String topic;

    @Async("userLogTaskExecutor")
    public void publishBatch(Long memberId, List<UserLogRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        customerMetrics.recordUserLogBatchSize(requests.size());
        batchSizeSummary.record(requests.size());
        requestCounter("batch").increment();
        for (UserLogRequest request : requests) {
            doPublish(memberId, request);
        }

        requests.forEach(request -> sendAdminTarget(memberId, request));
    }

    @Async("userLogTaskExecutor")
    public void publish(Long memberId, UserLogRequest request) {
        requestCounter("single").increment();
        doPublish(memberId, request);
        sendAdminTarget(memberId, request);
    }

    private void doPublish(Long memberId, UserLogRequest request) {
        UserLogEventName eventName = UserLogEventName.from(request.eventName());

        long eventId;
        try {
            eventId = decodeTsidToLong(request.tsid());
        } catch (IllegalArgumentException e) {
            customerMetrics.recordUserLogPublish(request.eventName(), "invalid_tsid");
            resultCounter("invalid_tsid").increment();
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
            resultCounter("serialization_error").increment();
            log.warn("[UserLog] 직렬화 실패 memberId={}", memberId, e);
            return;
        }

        kafkaTemplate.send(topic, String.valueOf(memberId), json)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        customerMetrics.recordUserLogPublish(eventName.value(), "kafka_error");
                        resultCounter("kafka_error").increment();
                        log.warn("[UserLog] Kafka 전송 실패 memberId={} eventName={}",
                                memberId, eventName.value(), ex);
                        return;
                    }
                    customerMetrics.recordUserLogPublish(eventName.value(), "success");
                    resultCounter("kafka_success").increment();
                });
    }

    private static long decodeTsidToLong(String tsid) {
        if (tsid == null || tsid.isBlank()) {
            throw new IllegalArgumentException("TSID must not be null or blank");
        }
        Tsid parsed = Tsid.from(tsid);
        return parsed.toLong();
    }

    private void sendAdminTarget(Long memberId, UserLogRequest request) {
        UserLogEventName eventName = UserLogEventName.from(request.eventName());
        if (!isAdminTarget(eventName)) {
            return;
        }

        adminLogFeaturesClient.sendLogFeature(
                memberId,
                eventName,
                request.timestamp()
        );
    }

    private boolean isAdminTarget(UserLogEventName eventName) {
        return eventName == UserLogEventName.CLICK_COMPARE
                || eventName == UserLogEventName.CLICK_PENALTY
                || eventName == UserLogEventName.CLICK_CHANGE;
    }

    private Counter requestCounter(String mode) {
        return requestCounters.computeIfAbsent(mode, ignored ->
                Counter.builder("holliverse.userlog.requests")
                        .description("User log request count by mode")
                        .tag("mode", mode)
                        .register(meterRegistry));
    }

    private Counter resultCounter(String result) {
        return resultCounters.computeIfAbsent(result, ignored ->
                Counter.builder("holliverse.userlog.publish")
                        .description("User log publish result count")
                        .tag("result", result)
                        .register(meterRegistry));
    }
}
