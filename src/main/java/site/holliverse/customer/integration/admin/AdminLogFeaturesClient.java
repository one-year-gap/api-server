package site.holliverse.customer.integration.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import site.holliverse.customer.config.AdminLogFeaturesProperties;

/**
 * Admin API POST /api/v1/admin/log-features 호출용 클라이언트.
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
        this.baseUrl = (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
        this.logFeaturesPath = properties.logFeaturesPath();
    }

    /**
     * log-features API 호출. baseUrl이 비어 있으면 호출하지 않음(no-op).
     * 실패 시 로깅만 하고 예외 전파하지 않음(배치 처리 방해 방지).
     */
    public void sendLogFeatures(long memberId, int comparisonIncrement, int penaltyIncrement) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return;
        }
        String path = (logFeaturesPath != null && !logFeaturesPath.isBlank())
                ? logFeaturesPath
                : "/api/v1/admin/log-features";
        String url = baseUrl + path;
        LogFeaturesRequestBody body = new LogFeaturesRequestBody(memberId, comparisonIncrement, penaltyIncrement);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LogFeaturesRequestBody> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("[AdminLogFeatures] POST {} memberId={} status={}", url, memberId, response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.warn("[AdminLogFeatures] POST {} memberId={} failed", url, memberId, e);
        }
    }

    /** Admin API 요청 body (admin 패키지 미참조). */
    public record LogFeaturesRequestBody(long memberId, int comparisonIncrement, int penaltyIncrement) {}
}
