package site.holliverse.customer.application.usecase.recommendation;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.persistence.entity.PersonaRecommendation;
import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.customer.persistence.repository.PersonaRecommendationRepository;
import site.holliverse.shared.domain.model.PersonaSegment;

import java.util.List;

/**
 * 추천 캐시 upsert를 트랜잭션 경계 내에서만 수행하는 전용 서비스.
 */
@Service
@Profile("customer")
public class RecommendationTxService {

    private final PersonaRecommendationRepository personaRecommendationRepository;

    public RecommendationTxService(PersonaRecommendationRepository personaRecommendationRepository) {
        this.personaRecommendationRepository = personaRecommendationRepository;
    }

    @Transactional
    public PersonaRecommendation upsert(Long memberId, PersonaSegment segment, String cachedLlmRecommendation,
                                        List<RecommendedProductItem> items) {
        return personaRecommendationRepository.findById(memberId)
                .map(existing -> {
                    existing.updateRecommendation(segment, cachedLlmRecommendation, items);
                    return personaRecommendationRepository.save(existing);
                })
                .orElseGet(() -> personaRecommendationRepository.save(
                        PersonaRecommendation.builder()
                                .memberId(memberId)
                                .segment(segment)
                                .cachedLlmRecommendation(cachedLlmRecommendation)
                                .recommendedProducts(items)
                                .build()
                ));
    }
}
