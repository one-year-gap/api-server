package site.holliverse.customer.application.usecase.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import site.holliverse.customer.integration.fastapi.FastApiRecommendationClient;

/**
 * 추천 캐시 미스 시 FastAPI 호출만 수행하는 비동기 트리거.
 * FastAPI가 persona_recommendation에 직접 적재하므로 Spring에서는 호출만 하고 응답은 사용하지 않음.
 */
@Slf4j
@Component
@Profile("customer")
@RequiredArgsConstructor
public class RecommendationRefreshTrigger {

    private static final String ASYNC_EXECUTOR = "userLogTaskExecutor";

    private final FastApiRecommendationClient fastApiRecommendationClient;

    @Async(ASYNC_EXECUTOR)
    public void triggerFetchAndStore(Long memberId) {
        try {
            fastApiRecommendationClient.fetchRecommendation(memberId);
            log.debug("[Recommendation] FastAPI 비동기 호출 완료 memberId={}", memberId);
        } catch (RestClientException e) {
            log.warn("[Recommendation] FastAPI 비동기 호출 실패 memberId={} message={}",
                    memberId, e.getMessage());
        }
    }
}
