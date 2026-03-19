package site.holliverse.customer.integration.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import site.holliverse.customer.config.AdminLogFeaturesProperties;
import site.holliverse.customer.application.usecase.log.UserLogEventName;

/**
 * Admin API POST /internal/v1/log-features 호출용 클라이언트.
 * customer 모듈은 admin 패키지를 의존하지 않고 HTTP만 사용(ArchUnit 준수).
 */
@Slf4j
public class AdminLogFeaturesClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String logFeaturesPath;

    public AdminLogFeaturesClient(RestTemplate restTemplate, AdminLogFeaturesProperties properties) {
        this.restTemplate = restTemplate;
        String url = properties.baseUrl();
        this.baseUrl =  url;
        this.logFeaturesPath = properties.logFeaturesPath();
    }

    /**
     * log-features API 호출. baseUrl이 비어 있으면 호출하지 않음(no-op).
     * 실패 시 로깅만 하고 예외 전파하지 않음(배치 처리 방해 방지).
     */
    public void sendLogFeature(long memberId, UserLogEventName eventType, String timeStamp) {

        String path =  logFeaturesPath;
        String url = baseUrl + path;

        LogFeatureRequestBody body = new LogFeatureRequestBody(eventType.value(), memberId, timeStamp);
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LogFeatureRequestBody> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("[AdminLogFeatures] POST {} memberId={} eventType={} status={}",
                        url, memberId, eventType.value(), response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.warn("[AdminLogFeatures] POST {} memberId={} eventType={} failed",
                    url, memberId, eventType.value(), e);
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
}
