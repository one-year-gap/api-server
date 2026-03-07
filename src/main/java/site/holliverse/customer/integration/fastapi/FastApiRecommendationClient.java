package site.holliverse.customer.integration.fastapi;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import site.holliverse.customer.config.FastApiProperties;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendationRequest;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendationResponse;

/**
 * FastAPI(LLM 추천) 호출용 클라이언트. RestTemplate 동기 호출.
 */
@Component
@Profile("customer")
public class FastApiRecommendationClient {

    private static final String RECOMMEND_PATH = "/api/v1/recommendations";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FastApiRecommendationClient(
            @Qualifier("fastApiRestTemplate") RestTemplate restTemplate,
            FastApiProperties fastApiProperties) {
        this.restTemplate = restTemplate;
        String url = fastApiProperties.baseUrl();
        this.baseUrl = url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * 회원 ID로 추천 요청. FastAPI가 segment, 캐시 문구, 추천 상품 목록 반환.
     */
    public FastApiRecommendationResponse fetchRecommendation(Long memberId) {
        String url = baseUrl + RECOMMEND_PATH;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FastApiRecommendationRequest> request = new HttpEntity<>(
                new FastApiRecommendationRequest(memberId),
                headers
        );
        return restTemplate.postForObject(url, request, FastApiRecommendationResponse.class);
    }
}
