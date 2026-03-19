package site.holliverse.customer.application.usecase.recommendation;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 캐시 미스 시 GET 추천 요청이 Kafka 결과를 기다리기 위한 CompletableFuture 레지스트리.
 * member_id당 하나의 Future만 보관하여 동시 동일 회원 요청이 같은 결과를 기다리도록 함.
 */
@Component
@Profile("customer")
public class RecommendationPendingFutureRegistry {

    private final ConcurrentHashMap<Long, CompletableFuture<RecommendationResult>> pending = new ConcurrentHashMap<>();

    public RecommendationPendingFutureRegistry(MeterRegistry meterRegistry) {
        Gauge.builder("holliverse.recommendation.pending.size", pending, Map::size)
                .description("Current number of pending recommendation futures")
                .register(meterRegistry);
    }

    /**
     * 해당 회원에 대한 대기 Future를 반환하거나, 없으면 새로 생성해 등록 후 반환.
     */
    public CompletableFuture<RecommendationResult> getOrCreate(Long memberId) {
        return pending.computeIfAbsent(memberId, k -> new CompletableFuture<>());
    }

    /**
     * 회원에 대한 Future를 제거하고 반환.
     * Kafka Consumer는 remove 후 complete(result) 호출.
     * 타임아웃/실패 시 서비스는 remove 후 completeExceptionally 호출하여 메모리 누수 방지.
     */
    public CompletableFuture<RecommendationResult> remove(Long memberId) {
        return pending.remove(memberId);
    }
}
