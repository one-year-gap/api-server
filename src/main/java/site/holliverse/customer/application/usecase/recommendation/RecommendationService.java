package site.holliverse.customer.application.usecase.recommendation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.customer.integration.fastapi.FastApiRecommendationClient;
import site.holliverse.customer.persistence.entity.PersonaRecommendation;
import site.holliverse.customer.persistence.repository.PersonaRecommendationRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    public RecommendationService(MemberRepository memberRepository,
                                PersonaRecommendationRepository personaRecommendationRepository,
                                FastApiRecommendationClient fastApiRecommendationClient,
                                RecommendationPendingFutureRegistry pendingFutureRegistry,
                                @Value("${app.recommendation.await-timeout-seconds:90}") long awaitTimeoutSeconds) {
        this.memberRepository = memberRepository;
        this.personaRecommendationRepository = personaRecommendationRepository;
        this.fastApiRecommendationClient = fastApiRecommendationClient;
        this.pendingFutureRegistry = pendingFutureRegistry;
        this.recommendationTaskExecutor = new SameThreadExecutor();
        this.awaitTimeoutSeconds = awaitTimeoutSeconds;
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
                .map(entity -> toResult(entity, RecommendationResult.RecommendationSource.CACHE));

        if (cached.isPresent()) {
            return cached.get();
        }

        CompletableFuture<RecommendationResult> future = pendingFutureRegistry.getOrCreate(memberId);
        recommendationTaskExecutor.execute(() -> {
            try {
                fastApiRecommendationClient.triggerRecommendation(memberId);
            } catch (Exception e) {
                CompletableFuture<RecommendationResult> removed = pendingFutureRegistry.remove(memberId);
                if (removed != null) {
                    removed.completeExceptionally(e);
                }
            }
        });

        try {
            return future.get(awaitTimeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pendingFutureRegistry.remove(memberId);
            return RecommendationResult.pending(PENDING_MESSAGE);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            pendingFutureRegistry.remove(memberId);
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

    static RecommendationResult toResult(PersonaRecommendation entity,
                                         RecommendationResult.RecommendationSource source) {
        return new RecommendationResult(
                entity.getSegment(),
                entity.getCachedLlmRecommendation(),
                new ArrayList<>(entity.getRecommendedProducts()),
                source,
                entity.getUpdatedAt()
        );
    }

    /** 동기 트리거 후 Future 대기이므로, 호출 스레드에서 FastAPI 호출만 별도 실행할 때 사용. */
    private static final class SameThreadExecutor implements Executor {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }
}
