package site.holliverse.customer.application.usecase.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.integration.fastapi.FastApiRecommendationClient;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendationResponse;
import site.holliverse.customer.integration.fastapi.dto.FastApiRecommendedProductItem;
import site.holliverse.customer.persistence.entity.PersonaRecommendation;
import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.customer.persistence.repository.PersonaRecommendationRepository;
import site.holliverse.shared.domain.model.PersonaSegment;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PersonaRecommendationRepository personaRecommendationRepository;
    @Mock
    private FastApiRecommendationClient fastApiRecommendationClient;

    @InjectMocks
    private RecommendationService recommendationService;

    private static final Long MEMBER_ID = 1L;

    @Nested
    @DisplayName("getRecommendations (캐시 우선)")
    class GetRecommendations {

        @Test
        @DisplayName("캐시 hit 시 FastAPI 미호출, CACHE 소스 반환")
        void cacheHit_doesNotCallFastApi_returnsCacheSource() {
            when(memberRepository.existsById(MEMBER_ID)).thenReturn(true);
            PersonaRecommendation cached = validCachedEntity(MEMBER_ID);
            when(personaRecommendationRepository.findById(MEMBER_ID)).thenReturn(Optional.of(cached));

            RecommendationResult result = recommendationService.getRecommendations(MEMBER_ID);

            assertThat(result.source()).isEqualTo(RecommendationResult.RecommendationSource.CACHE);
            assertThat(result.segment()).isEqualTo(PersonaSegment.normal);
            verify(fastApiRecommendationClient, never()).fetchRecommendation(any());
        }

        @Test
        @DisplayName("캐시 miss 시 FastAPI 호출 후 저장·반환, FASTAPI 소스")
        void cacheMiss_callsFastApi_savesAndReturnsFastApiSource() {
            when(memberRepository.existsById(MEMBER_ID)).thenReturn(true);
            when(personaRecommendationRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());
            FastApiRecommendationResponse apiResponse = new FastApiRecommendationResponse(
                    PersonaSegment.upsell,
                    "추천 문구",
                    List.of(new FastApiRecommendedProductItem(10L, "이유1"))
            );
            when(fastApiRecommendationClient.fetchRecommendation(MEMBER_ID)).thenReturn(apiResponse);
            PersonaRecommendation saved = PersonaRecommendation.builder()
                    .memberId(MEMBER_ID)
                    .segment(apiResponse.segment())
                    .cachedLlmRecommendation(apiResponse.cachedLlmRecommendation())
                    .recommendedProducts(List.of(new RecommendedProductItem(10L, "이유1")))
                    .updatedAt(Instant.now())
                    .build();
            when(personaRecommendationRepository.save(any())).thenReturn(saved);

            RecommendationResult result = recommendationService.getRecommendations(MEMBER_ID);

            assertThat(result.source()).isEqualTo(RecommendationResult.RecommendationSource.FASTAPI);
            verify(fastApiRecommendationClient).fetchRecommendation(MEMBER_ID);
            verify(personaRecommendationRepository).save(any(PersonaRecommendation.class));
        }

        @Test
        @DisplayName("회원 없으면 MEMBER_NOT_FOUND")
        void memberNotFound_throws() {
            when(memberRepository.existsById(MEMBER_ID)).thenReturn(false);

            assertThatThrownBy(() -> recommendationService.getRecommendations(MEMBER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("멤버");
        }
    }

    @Nested
    @DisplayName("refreshRecommendations (강제 갱신)")
    class RefreshRecommendations {

        @Test
        @DisplayName("항상 FastAPI 호출 후 저장·반환")
        void alwaysCallsFastApi_savesAndReturns() {
            when(memberRepository.existsById(MEMBER_ID)).thenReturn(true);
            FastApiRecommendationResponse apiResponse = new FastApiRecommendationResponse(
                    PersonaSegment.churn_risk,
                    "채널이탈 추천",
                    List.of(new FastApiRecommendedProductItem(20L, "이유2"))
            );
            when(fastApiRecommendationClient.fetchRecommendation(MEMBER_ID)).thenReturn(apiResponse);
            when(personaRecommendationRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());
            PersonaRecommendation saved = PersonaRecommendation.builder()
                    .memberId(MEMBER_ID)
                    .segment(apiResponse.segment())
                    .cachedLlmRecommendation(apiResponse.cachedLlmRecommendation())
                    .recommendedProducts(List.of(new RecommendedProductItem(20L, "이유2")))
                    .updatedAt(Instant.now())
                    .build();
            when(personaRecommendationRepository.save(any())).thenReturn(saved);

            RecommendationResult result = recommendationService.refreshRecommendations(MEMBER_ID);

            assertThat(result.source()).isEqualTo(RecommendationResult.RecommendationSource.FASTAPI);
            assertThat(result.segment()).isEqualTo(PersonaSegment.churn_risk);
            verify(fastApiRecommendationClient).fetchRecommendation(MEMBER_ID);
            verify(personaRecommendationRepository).save(any(PersonaRecommendation.class));
        }
    }

    private static PersonaRecommendation validCachedEntity(Long memberId) {
        return PersonaRecommendation.builder()
                .memberId(memberId)
                .segment(PersonaSegment.normal)
                .cachedLlmRecommendation("캐시 문구")
                .recommendedProducts(List.of(new RecommendedProductItem(1L, "이유")))
                .updatedAt(Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS))
                .build();
    }
}
