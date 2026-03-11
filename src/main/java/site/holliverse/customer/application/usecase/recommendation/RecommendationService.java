package site.holliverse.customer.application.usecase.recommendation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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

/**
 * 추천 조회(캐시 우선) 및 강제 갱신 서비스.
 * FastAPI가 persona_recommendation에 직접 적재하므로 Spring은 호출만 하고 응답 body로 DB에 쓰지 않음.
 */
@Service
@Profile("customer")
public class RecommendationService {

    private static final int CACHE_TTL_DAYS = 7;
    private static final String PENDING_MESSAGE = "추천을 생성 중입니다. 잠시 후 다시 조회해 주세요.";

    private final MemberRepository memberRepository;
    private final PersonaRecommendationRepository personaRecommendationRepository;
    private final FastApiRecommendationClient fastApiRecommendationClient;
    private final RecommendationRefreshTrigger recommendationRefreshTrigger;
    private final int refreshPollDelayMs;

    public RecommendationService(MemberRepository memberRepository,
                                PersonaRecommendationRepository personaRecommendationRepository,
                                FastApiRecommendationClient fastApiRecommendationClient,
                                RecommendationRefreshTrigger recommendationRefreshTrigger,
                                @Value("${app.recommendation.refresh-poll-delay-ms:20000}") int refreshPollDelayMs) {
        this.memberRepository = memberRepository;
        this.personaRecommendationRepository = personaRecommendationRepository;
        this.fastApiRecommendationClient = fastApiRecommendationClient;
        this.recommendationRefreshTrigger = recommendationRefreshTrigger;
        this.refreshPollDelayMs = refreshPollDelayMs;
    }

    /**
     * 캐시 우선: DB에 유효 캐시가 있으면 반환, 없으면 FastAPI 비동기 호출만 하고 즉시 PENDING 메시지 반환.
     */
    public RecommendationResult getRecommendations(Long memberId) {
        ensureMemberExists(memberId);

        Instant cacheExpiry = Instant.now().minus(CACHE_TTL_DAYS, ChronoUnit.DAYS);
        return personaRecommendationRepository.findById(memberId)
                .filter(entity -> entity.getUpdatedAt().isAfter(cacheExpiry))
                .map(entity -> toResult(entity, RecommendationResult.RecommendationSource.CACHE))
                .orElseGet(() -> {
                    recommendationRefreshTrigger.triggerFetchAndStore(memberId);
                    return RecommendationResult.pending(PENDING_MESSAGE);
                });
    }

    /**
     * 강제 갱신: FastAPI 동기 호출 후 대기 후 DB 조회해 반환. 없으면 PENDING 메시지 반환.
     */
    public RecommendationResult refreshRecommendations(Long memberId) {
        ensureMemberExists(memberId);

        try {
            fastApiRecommendationClient.fetchRecommendation(memberId);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.RECOMMENDATION_UNAVAILABLE, "fastapi",
                    "추천 서비스 호출에 실패했습니다.", e.getMessage());
        }

        if (refreshPollDelayMs > 0) {
            try {
                Thread.sleep(refreshPollDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return RecommendationResult.pending(PENDING_MESSAGE);
            }
        }

        Optional<PersonaRecommendation> entity = personaRecommendationRepository.findById(memberId);
        return entity
                .map(e -> toResult(e, RecommendationResult.RecommendationSource.FASTAPI))
                .orElseGet(() -> RecommendationResult.pending(PENDING_MESSAGE));
    }

    private void ensureMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "memberId", "멤버를 찾을 수 없습니다.");
        }
    }

    private static RecommendationResult toResult(PersonaRecommendation entity,
                                                 RecommendationResult.RecommendationSource source) {
        return new RecommendationResult(
                entity.getSegment(),
                entity.getCachedLlmRecommendation(),
                new ArrayList<>(entity.getRecommendedProducts()),
                source,
                entity.getUpdatedAt()
        );
    }
}
