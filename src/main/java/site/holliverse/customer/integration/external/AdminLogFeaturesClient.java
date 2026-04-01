package site.holliverse.customer.integration.external;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import site.holliverse.customer.config.AdminLogFeaturesProperties;
import site.holliverse.customer.application.usecase.log.UserLogEventName;
import site.holliverse.shared.monitoring.CustomerMetrics;

import java.util.List;

/**
 * Admin API POST /internal/v1/log-features 호출용 클라이언트.
 * customer 모듈은 admin 패키지를 의존하지 않고 HTTP만 사용(ArchUnit 준수).
 */
@Slf4j
public class AdminLogFeaturesClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String logFeaturesPath;
    private final String logFeaturesBatchPath;
    private final CustomerMetrics customerMetrics;

    public AdminLogFeaturesClient(
            RestTemplate restTemplate,
            AdminLogFeaturesProperties properties,
            CustomerMetrics customerMetrics
    ) {
        this.restTemplate = restTemplate;
        String url = properties.baseUrl();
        this.baseUrl =  url;
        this.logFeaturesPath = properties.logFeaturesPath();
        this.logFeaturesBatchPath = properties.logFeaturesBatchPath();
        this.customerMetrics = customerMetrics;
    }

    /**
     * log-features API 호출. baseUrl이 비어 있으면 호출하지 않음(no-op).
     * 실패 시 결과 객체로 반환하고 예외 전파하지 않음(재시도 경로 위임).
     */
    public DispatchResult sendLogFeature(long memberId, UserLogEventName eventType, String timeStamp) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return DispatchResult.ok();
        }

        String url = baseUrl + logFeaturesPath;

        LogFeatureRequestBody body = new LogFeatureRequestBody(eventType.value(), memberId, timeStamp);
        var timerSample = customerMetrics.startSample();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    jsonEntity(body),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                customerMetrics.stopAdminLogFeatureDuration(timerSample, "non_2xx", "single");
                log.warn("[AdminLogFeatures] POST {} memberId={} eventType={} status={}",
                        url, memberId, eventType.value(), response.getStatusCode());
                return DispatchResult.fail("non_2xx:" + response.getStatusCode().value());
            }
            customerMetrics.stopAdminLogFeatureDuration(timerSample, "success", "single");
            return DispatchResult.ok();
        } catch (RestClientException e) {
            customerMetrics.stopAdminLogFeatureDuration(timerSample, "error", "single");
            log.warn("[AdminLogFeatures] POST {} memberId={} eventType={} failed",
                    url, memberId, eventType.value(), e);
            return DispatchResult.fail(e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }

    public DispatchResult sendLogFeaturesBatch(long memberId, List<BatchLogEvent> events) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return DispatchResult.ok();
        }
        if (events == null || events.isEmpty()) {
            return DispatchResult.ok();
        }

        String url = baseUrl + logFeaturesBatchPath;
        customerMetrics.recordAdminLogFeatureBatchSize(events.size());
        var timerSample = customerMetrics.startSample();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    jsonEntity(new BatchLogFeatureRequestBody(memberId, events)),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                customerMetrics.stopAdminLogFeatureDuration(timerSample, "non_2xx", "batch");
                log.warn("[AdminLogFeatures][Batch] POST {} memberId={} size={} status={}",
                        url, memberId, events.size(), response.getStatusCode());
                return DispatchResult.fail("non_2xx:" + response.getStatusCode().value());
            }

            customerMetrics.stopAdminLogFeatureDuration(timerSample, "success", "batch");
            return DispatchResult.ok();
        } catch (RestClientException e) {
            customerMetrics.stopAdminLogFeatureDuration(timerSample, "error", "batch");
            log.warn("[AdminLogFeatures][Batch] POST {} memberId={} size={} failed",
                    url, memberId, events.size(), e);
            return DispatchResult.fail(e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }

    private HttpEntity<?> jsonEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    public record DispatchResult(boolean success, String errorMessage) {
        public static DispatchResult ok() {
            return new DispatchResult(true, null);
        }

        public static DispatchResult fail(String errorMessage) {
            return new DispatchResult(false, errorMessage);
        }
    }

    /**
     * Admin API 요청 body.
     */
    public record LogFeatureRequestBody(
            String eventType,
            long memberId,
            String timeStamp
    ) {
    }

    public record BatchLogFeatureRequestBody(
            long memberId,
            List<BatchLogEvent> events
    ) {
    }

    public record BatchLogEvent(
            long eventId,
            String timestamp,
            String event,
            String eventName,
            JsonNode eventProperties
    ) {
    }
}
