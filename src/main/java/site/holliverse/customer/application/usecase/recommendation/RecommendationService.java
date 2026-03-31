package site.holliverse.customer.application.usecase.recommendation;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.customer.error.CustomerErrorCode;
import site.holliverse.customer.error.CustomerException;
import site.holliverse.customer.integration.fastapi.FastApiRecommendationClient;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendationResponse;
import site.holliverse.customer.persistence.entity.PersonaRecommendation;
import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.customer.persistence.repository.PersonaRecommendationRepository;
import site.holliverse.infra.error.InfraErrorCode;
import site.holliverse.infra.error.InfraException;
import site.holliverse.shared.error.DomainException;
import site.holliverse.shared.monitoring.CustomerMetrics;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private final CustomerMetrics customerMetrics;
    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> recommendationCacheCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> recommendationFastApiCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> recommendationFinalCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> recommendationWaitTimers = new ConcurrentHashMap<>();
    private final Map<String, Counter> recommendationFastApiErrorCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> recommendationErrorCounters = new ConcurrentHashMap<>();

    public RecommendationService(MemberRepository memberRepository,
                                 PersonaRecommendationRepository personaRecommendationRepository,
                                 FastApiRecommendationClient fastApiRecommendationClient,
                                 RecommendationPendingFutureRegistry pendingFutureRegistry,
                                 @Qualifier("recommendationTaskExecutor") Executor recommendationTaskExecutor,
                                 CustomerMetrics customerMetrics,
                                 @Value("${app.recommendation.await-timeout-seconds:90}") long awaitTimeoutSeconds,
                                 MeterRegistry meterRegistry) {
        this.memberRepository = memberRepository;
        this.personaRecommendationRepository = personaRecommendationRepository;
        this.fastApiRecommendationClient = fastApiRecommendationClient;
        this.pendingFutureRegistry = pendingFutureRegistry;
        this.recommendationTaskExecutor = recommendationTaskExecutor;
        this.customerMetrics = customerMetrics;
        this.awaitTimeoutSeconds = awaitTimeoutSeconds;
        this.meterRegistry = meterRegistry;
    }

    public RecommendationResult getRecommendations(Long memberId) {
        var totalSample = customerMetrics.startSample();
        ensureMemberExists(memberId);

        Instant cacheExpiry = Instant.now().minus(CACHE_TTL_DAYS, ChronoUnit.DAYS);
        Optional<RecommendationResult> cached = personaRecommendationRepository.findById(memberId)
                .filter(entity -> entity.getUpdatedAt().isAfter(cacheExpiry))
                .map(entity -> RecommendationResult.fromEntity(entity, RecommendationResult.RecommendationSource.CACHE));

        if (cached.isPresent()) {
            customerMetrics.recordRecommendationRequest("cache_hit");
            customerMetrics.stopRecommendationDuration(totalSample, "success", "cache");
            recommendationCacheCounter("hit").increment();
            recommendationFinalCounter("cache").increment();
            return cached.get();
        }

        customerMetrics.recordRecommendationRequest("cache_miss");
        recommendationCacheCounter("miss").increment();
        CompletableFuture<RecommendationResult> future = pendingFutureRegistry.getOrCreate(memberId);
        recommendationTaskExecutor.execute(() -> {
            try {
                Optional<FastApiRecommendationResponse> syncResponse = fastApiRecommendationClient.triggerRecommendation(memberId);
                if (syncResponse.isPresent()) {
                    recommendationFastApiCounter("sync").increment();
                    customerMetrics.recordRecommendationTrigger("sync");
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
                    customerMetrics.recordRecommendationTrigger("accepted");
                }
            } catch (Exception e) {
                recommendationFastApiErrorCounter(e.getClass().getSimpleName()).increment();
                customerMetrics.recordRecommendationTrigger("error");
                CompletableFuture<RecommendationResult> removed = pendingFutureRegistry.remove(memberId);
                if (removed != null) {
                    removed.completeExceptionally(e);
                }
            }
        });

        Timer.Sample legacyWaitSample = Timer.start(meterRegistry);
        var waitSample = customerMetrics.startSample();
        try {
            RecommendationResult result = future.get(awaitTimeoutSeconds, TimeUnit.SECONDS);
            customerMetrics.recordRecommendationRequest("resolved");
            customerMetrics.stopRecommendationWaitDuration(waitSample, "success");
            customerMetrics.stopRecommendationDuration(totalSample, "success", result.source().name().toLowerCase());
            legacyWaitSample.stop(recommendationWaitTimer("completed"));
            recommendationFinalCounter("fastapi").increment();
            return result;
        } catch (TimeoutException e) {
            pendingFutureRegistry.remove(memberId);
            customerMetrics.recordRecommendationRequest("timeout");
            customerMetrics.stopRecommendationWaitDuration(waitSample, "timeout");
            customerMetrics.stopRecommendationDuration(totalSample, "timeout", "pending");
            legacyWaitSample.stop(recommendationWaitTimer("timeout"));
            recommendationFinalCounter("pending").increment();
            return RecommendationResult.pending(PENDING_MESSAGE);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            pendingFutureRegistry.remove(memberId);
            customerMetrics.recordRecommendationRequest("error");
            customerMetrics.stopRecommendationWaitDuration(waitSample, "error");
            customerMetrics.stopRecommendationDuration(totalSample, "error", "exception");
            legacyWaitSample.stop(recommendationWaitTimer("error"));
            recommendationErrorCounter(cause.getClass().getSimpleName()).increment();
            if (cause instanceof DomainException de) {
                throw de;
            }
            throw new InfraException(InfraErrorCode.RECOMMENDATION_UNAVAILABLE);
        }
    }

    private void ensureMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomerException(CustomerErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private Counter recommendationCacheCounter(String result) {
        return recommendationCacheCounters.computeIfAbsent(result, ignored ->
                Counter.builder("holliverse.recommendation.cache")
                        .description("Recommendation cache hit/miss count")
                        .tag("result", result)
                        .register(meterRegistry));
    }

    private Counter recommendationFastApiCounter(String type) {
        return recommendationFastApiCounters.computeIfAbsent(type, ignored ->
                Counter.builder("holliverse.recommendation.fastapi.responses")
                        .description("Recommendation FastAPI response type count")
                        .tag("type", type)
                        .register(meterRegistry));
    }

    private Counter recommendationFinalCounter(String result) {
        return recommendationFinalCounters.computeIfAbsent(result, ignored ->
                Counter.builder("holliverse.recommendation.final")
                        .description("Final recommendation API result count")
                        .tag("result", result)
                        .register(meterRegistry));
    }

    private Timer recommendationWaitTimer(String outcome) {
        return recommendationWaitTimers.computeIfAbsent(outcome, ignored ->
                Timer.builder("holliverse.recommendation.wait.duration")
                        .description("End-to-end wait time for cache-miss recommendation requests")
                        .tag("outcome", outcome)
                        .register(meterRegistry));
    }

    private Counter recommendationFastApiErrorCounter(String exception) {
        return recommendationFastApiErrorCounters.computeIfAbsent(exception, ignored ->
                Counter.builder("holliverse.recommendation.fastapi.errors")
                        .description("Recommendation trigger failures by exception")
                        .tag("exception", exception)
                        .register(meterRegistry));
    }

    private Counter recommendationErrorCounter(String exception) {
        return recommendationErrorCounters.computeIfAbsent(exception, ignored ->
                Counter.builder("holliverse.recommendation.errors")
                        .description("Recommendation request failures by exception")
                        .tag("exception", exception)
                        .register(meterRegistry));
    }
}
