package site.holliverse.customer.application.usecase.recommendation;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import site.holliverse.customer.integration.fastapi.FastApiRecommendationClient;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendationResponse;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendedProductItem;
import site.holliverse.customer.persistence.entity.PersonaRecommendation;
import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.customer.persistence.repository.PersonaRecommendationRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 추천 조회(캐시 우선) 및 강제 갱신(항상 FastAPI) 서비스.
 */
@Service
@Profile("customer")
public class RecommendationService {

    private static final int CACHE_TTL_DAYS = 7;

    private final MemberRepository memberRepository;
    private final PersonaRecommendationRepository personaRecommendationRepository;
    private final FastApiRecommendationClient fastApiRecommendationClient;
    private final RecommendationTxService recommendationTxService;

    public RecommendationService(MemberRepository memberRepository,
                                PersonaRecommendationRepository personaRecommendationRepository,
                                FastApiRecommendationClient fastApiRecommendationClient,
                                RecommendationTxService recommendationTxService) {
        this.memberRepository = memberRepository;
        this.personaRecommendationRepository = personaRecommendationRepository;
        this.fastApiRecommendationClient = fastApiRecommendationClient;
        this.recommendationTxService = recommendationTxService;
    }

    /**
     * 캐시 우선: DB에 유효 캐시가 있으면 반환, 없거나 만료 시 FastAPI 호출 후 저장·반환.
     */
    public RecommendationResult getRecommendations(Long memberId) {
        ensureMemberExists(memberId);

        Instant cacheExpiry = Instant.now().minus(CACHE_TTL_DAYS, ChronoUnit.DAYS);
        return personaRecommendationRepository.findById(memberId)
                .filter(entity -> entity.getUpdatedAt().isAfter(cacheExpiry))
                .map(entity -> toResult(entity, RecommendationResult.RecommendationSource.CACHE))
                .orElseGet(() -> fetchAndStore(memberId));
    }

    /**
     * 강제 갱신: 항상 FastAPI 호출 후 DB에 저장·반환.
     */
    public RecommendationResult refreshRecommendations(Long memberId) {
        ensureMemberExists(memberId);
        return fetchAndStore(memberId);
    }

    private void ensureMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "memberId", "멤버를 찾을 수 없습니다.");
        }
    }

    private RecommendationResult fetchAndStore(Long memberId) {
        FastApiRecommendationResponse response;
        try {
            response = fastApiRecommendationClient.fetchRecommendation(memberId);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.RECOMMENDATION_UNAVAILABLE, "fastapi",
                    "추천 서비스 호출에 실패했습니다.", e.getMessage());
        }
        if (response == null) {
            throw new CustomException(ErrorCode.RECOMMENDATION_UNAVAILABLE, "fastapi", "추천 서비스 응답이 비어 있습니다.");
        }

        List<RecommendedProductItem> items = mapToRecommendedProductItems(response.recommendedProducts());
        PersonaRecommendation saved = recommendationTxService.upsert(memberId, response.segment(),
                response.cachedLlmRecommendation(), items);
        return toResult(saved, RecommendationResult.RecommendationSource.FASTAPI);
    }

    private static List<RecommendedProductItem> mapToRecommendedProductItems(List<FastApiRecommendedProductItem> from) {
        if (from == null) return new ArrayList<>();
        return from.stream()
                .map(item -> new RecommendedProductItem(item.productId(), item.reason()))
                .toList();
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
