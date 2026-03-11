package site.holliverse.customer.integration.fastapi;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import site.holliverse.customer.config.FastApiProperties;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendationRequest;

/**
 * FastAPI(LLM 추천) 호출용 클라이언트. RestTemplate 동기 호출.
 * FastAPI는 요청 수신 즉시 202 Accepted를 반환하고, 결과는 Kafka로 발행.
 * Bean 등록은 IntegrationConfig에서 수행.
 */
public class FastApiRecommendationClient {

    private static final String RECOMMEND_PATH = "/api/v1/recommendations";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FastApiRecommendationClient(RestTemplate restTemplate, FastApiProperties fastApiProperties) {
        this.restTemplate = restTemplate;
        String url = fastApiProperties.baseUrl();
        this.baseUrl = url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * 회원 ID로 추천 요청 트리거. FastAPI는 202 Accepted를 즉시 반환하고,
     * 백그라운드에서 LLM 처리 후 결과를 Kafka recommendation-topic으로 발행.
     * 202가 아니면 예외를 던짐 (Spring에서 해당 Future completeExceptionally 처리).
     */
    public void triggerRecommendation(Long memberId) {
        String url = baseUrl + RECOMMEND_PATH;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FastApiRecommendationRequest> request = new HttpEntity<>(
                new FastApiRecommendationRequest(memberId),
                headers
        );
        var response = restTemplate.postForEntity(url, request, Void.class);
        if (response.getStatusCode() != HttpStatusCode.valueOf(202)) {
            throw new IllegalStateException(
                    "FastAPI expected 202 Accepted but got " + response.getStatusCode());
        }
    }
}
