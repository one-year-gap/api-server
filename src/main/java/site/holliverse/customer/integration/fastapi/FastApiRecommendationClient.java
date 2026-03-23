package site.holliverse.customer.integration.fastapi;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import site.holliverse.customer.config.FastApiProperties;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendationRequest;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendationResponse;
import site.holliverse.shared.monitoring.CustomerMetrics;

import java.util.Optional;

/**
 * FastAPI(LLM 추천) 호출용 클라이언트. RestTemplate 동기 호출.
 * 202: 비동기 수락(결과는 Kafka로 발행). 200: 동기 응답(body에 추천 결과).
 * Bean 등록은 IntegrationConfig에서 수행.
 */
public class FastApiRecommendationClient {

    private static final String RECOMMEND_PATH = "/api/v1/recommendations";

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final CustomerMetrics customerMetrics;

    public FastApiRecommendationClient(
            RestTemplate restTemplate,
            FastApiProperties fastApiProperties,
            CustomerMetrics customerMetrics
    ) {
        this.restTemplate = restTemplate;
        String url = fastApiProperties.baseUrl();
        this.baseUrl = url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.customerMetrics = customerMetrics;
    }

    /**
     * 회원 ID로 추천 요청.
     * - 202 Accepted: 비동기 처리(결과는 Kafka로 옴). body 없음 → empty 반환.
     * - 200 OK: 동기 응답 body 있음 → Optional.of(response) 반환.
     * - 그 외: 예외.
     */
    public Optional<FastApiRecommendationResponse> triggerRecommendation(Long memberId) {
        String url = baseUrl + RECOMMEND_PATH;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FastApiRecommendationRequest> request = new HttpEntity<>(
                new FastApiRecommendationRequest(memberId),
                headers
        );
        ResponseEntity<FastApiRecommendationResponse> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, request, FastApiRecommendationResponse.class);
        } catch (RestClientException ex) {
            customerMetrics.recordRecommendationTrigger("error");
            throw ex;
        }

        int code = response.getStatusCode().value();
        if (code == 202) {
            customerMetrics.recordRecommendationTrigger("accepted");
            return Optional.empty();
        }
        if (code == 200 && response.getBody() != null) {
            customerMetrics.recordRecommendationTrigger("sync");
            return Optional.of(response.getBody());
        }
        if (code == 200) {
            customerMetrics.recordRecommendationTrigger("empty_200");
            return Optional.empty();
        }
        customerMetrics.recordRecommendationTrigger("unexpected_status");
        throw new IllegalStateException(
                "FastAPI expected 200 or 202 but got " + response.getStatusCode());
    }
}
