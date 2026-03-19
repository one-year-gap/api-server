package site.holliverse.customer.application.usecase.recommendation;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.customer.integration.fastapi.FastApiRecommendationClient;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendationResponse;
import site.holliverse.customer.persistence.entity.PersonaRecommendation;
import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.customer.persistence.repository.PersonaRecommendationRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 추천 조회: 캐시 히트 시 즉시 반환, 캐시 미스 시 CompletableFuture로 대기 후 Kafka 수신 결과 반환.
 * FastAPI는 202 수신 후 Kafka로 결과 발행하고, Spring Consumer가 DB 적재 후 Future를 완료함.
 */
@Service
@Profile("customer")
public class RecommendationService {

    private static final int CACHE_TTL_DAYS = 7;
    private static final String PENDING_MESSAGE = "추천을 생성 중입니다. 잠시 후 다시 조회해 주세요.";

    private final MemberRepository memberRepository;
    private final PersonaRecommendationRepository personaRecommendationRepository;
    private final FastApiRecommendationClient fastApiRecommendationClient;
    private final RecommendationPendingFutureRegistry pendingFutureRegistry;
    private final Executor recommendationTaskExecutor;
    private final long awaitTimeoutSeconds;
    private final MeterRegistry meterRegistry;

    public RecommendationService(MemberRepository memberRepository,
                                PersonaRecommendationRepository personaRecommendationRepository,
                                FastApiRecommendationClient fastApiRecommendationClient,
                                RecommendationPendingFutureRegistry pendingFutureRegistry,
                                @Qualifier("recommendationTaskExecutor") Executor recommendationTaskExecutor,
                                @Value("${app.recommendation.await-timeout-seconds:90}") long awaitTimeoutSeconds,
                                MeterRegistry meterRegistry) {
        this.memberRepository = memberRepository;
        this.personaRecommendationRepository = personaRecommendationRepository;
        this.fastApiRecommendationClient = fastApiRecommendationClient;
        this.pendingFutureRegistry = pendingFutureRegistry;
        this.recommendationTaskExecutor = recommendationTaskExecutor;
        this.awaitTimeoutSeconds = awaitTimeoutSeconds;
        this.meterRegistry = meterRegistry;
    }

    /**
     * 캐시 우선: 유효 캐시 있으면 즉시 반환.
     * 캐시 미스: Future 등록 → FastAPI 202 트리거 → Future.get(timeout) 대기 후 반환. 타임아웃 시 PENDING 반환.
     */
    public RecommendationResult getRecommendations(Long memberId) {
        ensureMemberExists(memberId);

        Instant cacheExpiry = Instant.now().minus(CACHE_TTL_DAYS, ChronoUnit.DAYS);
        Optional<RecommendationResult> cached = personaRecommendationRepository.findById(memberId)
                .filter(entity -> entity.getUpdatedAt().isAfter(cacheExpiry))
                .map(entity -> RecommendationResult.fromEntity(entity, RecommendationResult.RecommendationSource.CACHE));

        if (cached.isPresent()) {
            recommendationCacheCounter("hit").increment();
            recommendationFinalCounter("cache").increment();
            return cached.get();
        }

        recommendationCacheCounter("miss").increment();

        CompletableFuture<RecommendationResult> future = pendingFutureRegistry.getOrCreate(memberId);
        Timer.Sample waitSample = Timer.start(meterRegistry);
        recommendationTaskExecutor.execute(() -> {
            try {
                Optional<FastApiRecommendationResponse> syncResponse = fastApiRecommendationClient.triggerRecommendation(memberId);
                if (syncResponse.isPresent()) {
                    recommendationFastApiCounter("sync").increment();
                    // 200 OK 동기 응답: DB 저장 후 Future 즉시 완료
                    FastApiRecommendationResponse r = syncResponse.get();
                    List<RecommendedProductItem> products = r.recommendedProducts() == null
                            ? Collections.emptyList()
                            : r.recommendedProducts().stream()
                            .map(p -> new RecommendedProductItem(
                                    p.rank(),
                                    p.productId(),
                                    p.productName(),
                                    p.productType(),
                                    p.productPrice(),
                                    p.salePrice(),
                                    p.tags() != null ? p.tags() : Collections.emptyList(),
                                    p.reason()
                            ))
                            .toList();
                    String cachedText = r.cachedLlmRecommendation() != null ? r.cachedLlmRecommendation() : "";
                    PersonaRecommendation entity = personaRecommendationRepository.findById(memberId)
                            .orElseGet(() -> PersonaRecommendation.builder()
                                    .memberId(memberId)
                                    .segment(r.segment())
                                    .cachedLlmRecommendation(cachedText)
                                    .recommendedProducts(products)
                                    .build());
                    entity.updateRecommendation(r.segment(), cachedText, products);
                    PersonaRecommendation saved = personaRecommendationRepository.save(entity);
                    CompletableFuture<RecommendationResult> removed = pendingFutureRegistry.remove(memberId);
                    if (removed != null) {
                        removed.complete(RecommendationResult.fromEntity(saved, RecommendationResult.RecommendationSource.FASTAPI));
                    }
                } else {
                    recommendationFastApiCounter("accepted").increment();
                }
            } catch (Exception e) {
                Counter.builder("holliverse.recommendation.fastapi.errors")
                        .description("Recommendation trigger failures by exception")
                        .tag("exception", e.getClass().getSimpleName())
                        .register(meterRegistry)
                        .increment();
                CompletableFuture<RecommendationResult> removed = pendingFutureRegistry.remove(memberId);
                if (removed != null) {
                    removed.completeExceptionally(e);
                }
            }
        });

        try {
            RecommendationResult result = future.get(awaitTimeoutSeconds, TimeUnit.SECONDS);
            waitSample.stop(recommendationWaitTimer("completed"));
            recommendationFinalCounter("fastapi").increment();
            return result;
        } catch (TimeoutException e) {
            pendingFutureRegistry.remove(memberId);
            waitSample.stop(recommendationWaitTimer("timeout"));
            recommendationFinalCounter("pending").increment();
            return RecommendationResult.pending(PENDING_MESSAGE);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            pendingFutureRegistry.remove(memberId);
            waitSample.stop(recommendationWaitTimer("error"));
            Counter.builder("holliverse.recommendation.errors")
                    .description("Recommendation request failures by exception")
                    .tag("exception", cause.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            if (cause instanceof CustomException ce) {
                throw ce;
            }
            throw new CustomException(ErrorCode.RECOMMENDATION_UNAVAILABLE, "fastapi",
                    "추천 서비스 호출에 실패했습니다.", cause.getMessage());
        }
    }

    private void ensureMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "memberId", "멤버를 찾을 수 없습니다.");
        }
    }

    private Counter recommendationCacheCounter(String result) {
        return Counter.builder("holliverse.recommendation.cache")
                .description("Recommendation cache hit/miss count")
                .tag("result", result)
                .register(meterRegistry);
    }

    private Counter recommendationFastApiCounter(String type) {
        return Counter.builder("holliverse.recommendation.fastapi.responses")
                .description("Recommendation FastAPI response type count")
                .tag("type", type)
                .register(meterRegistry);
    }

    private Counter recommendationFinalCounter(String result) {
        return Counter.builder("holliverse.recommendation.final")
                .description("Final recommendation API result count")
                .tag("result", result)
                .register(meterRegistry);
    }

    private Timer recommendationWaitTimer(String outcome) {
        return Timer.builder("holliverse.recommendation.wait.duration")
                .description("End-to-end wait time for cache-miss recommendation requests")
                .tag("outcome", outcome)
                .register(meterRegistry);
    }

}
