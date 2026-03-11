package site.holliverse.customer.application.usecase.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.integration.fastapi.FastApiRecommendationClient;
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

    private static final int NO_DELAY_MS = 0;
    private static final Long MEMBER_ID = 1L;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PersonaRecommendationRepository personaRecommendationRepository;
    @Mock
    private FastApiRecommendationClient fastApiRecommendationClient;
    @Mock
    private RecommendationRefreshTrigger recommendationRefreshTrigger;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(
                memberRepository,
                personaRecommendationRepository,
                fastApiRecommendationClient,
                recommendationRefreshTrigger,
                NO_DELAY_MS
        );
    }

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
            assertThat(result.segment()).isEqualTo(PersonaSegment.NORMAL);
            verify(fastApiRecommendationClient, never()).fetchRecommendation(any());
        }

        @Test
        @DisplayName("캐시 miss 시 비동기 트리거만 호출하고 즉시 PENDING 반환")
        void cacheMiss_triggersAsync_returnsPending() {
            when(memberRepository.existsById(MEMBER_ID)).thenReturn(true);
            when(personaRecommendationRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

            RecommendationResult result = recommendationService.getRecommendations(MEMBER_ID);

            assertThat(result.source()).isEqualTo(RecommendationResult.RecommendationSource.PENDING);
            assertThat(result.recommendedProducts()).isEmpty();
            assertThat(result.cachedLlmRecommendation()).contains("생성 중");
            verify(recommendationRefreshTrigger).triggerFetchAndStore(MEMBER_ID);
            verify(fastApiRecommendationClient, never()).fetchRecommendation(any());
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
        @DisplayName("FastAPI 동기 호출 후 DB 조회해 FASTAPI 소스 반환")
        void callsFastApi_thenReadsDb_returnsFastApiSource() {
            when(memberRepository.existsById(MEMBER_ID)).thenReturn(true);
            when(fastApiRecommendationClient.fetchRecommendation(MEMBER_ID)).thenReturn(null);
            PersonaRecommendation saved = PersonaRecommendation.builder()
                    .memberId(MEMBER_ID)
                    .segment(PersonaSegment.CHURN_RISK)
                    .cachedLlmRecommendation("채널이탈 추천")
                    .recommendedProducts(List.of(new RecommendedProductItem(20L, "이유2")))
                    .updatedAt(Instant.now())
                    .build();
            when(personaRecommendationRepository.findById(MEMBER_ID)).thenReturn(Optional.of(saved));

            RecommendationResult result = recommendationService.refreshRecommendations(MEMBER_ID);

            assertThat(result.source()).isEqualTo(RecommendationResult.RecommendationSource.FASTAPI);
            assertThat(result.segment()).isEqualTo(PersonaSegment.CHURN_RISK);
            verify(fastApiRecommendationClient).fetchRecommendation(MEMBER_ID);
            verify(personaRecommendationRepository).findById(MEMBER_ID);
        }

        @Test
        @DisplayName("FastAPI 호출 후 DB에 없으면 PENDING 반환")
        void callsFastApi_dbEmpty_returnsPending() {
            when(memberRepository.existsById(MEMBER_ID)).thenReturn(true);
            when(fastApiRecommendationClient.fetchRecommendation(MEMBER_ID)).thenReturn(null);
            when(personaRecommendationRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

            RecommendationResult result = recommendationService.refreshRecommendations(MEMBER_ID);

            assertThat(result.source()).isEqualTo(RecommendationResult.RecommendationSource.PENDING);
            assertThat(result.recommendedProducts()).isEmpty();
        }
    }

    private static PersonaRecommendation validCachedEntity(Long memberId) {
        return PersonaRecommendation.builder()
                .memberId(memberId)
                .segment(PersonaSegment.NORMAL)
                .cachedLlmRecommendation("캐시 문구")
                .recommendedProducts(List.of(new RecommendedProductItem(1L, "이유")))
                .updatedAt(Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS))
                .build();
    }
}
