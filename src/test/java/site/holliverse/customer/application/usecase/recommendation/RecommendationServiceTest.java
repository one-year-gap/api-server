package site.holliverse.customer.application.usecase.recommendation;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.error.CustomerErrorCode;
import site.holliverse.customer.error.CustomerException;
import site.holliverse.customer.integration.fastapi.FastApiRecommendationClient;
import site.holliverse.customer.persistence.entity.PersonaRecommendation;
import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.customer.persistence.repository.PersonaRecommendationRepository;
import site.holliverse.shared.domain.model.PersonaSegment;
import site.holliverse.shared.monitoring.CustomerMetrics;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    private static final Long MEMBER_ID = 1L;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PersonaRecommendationRepository personaRecommendationRepository;
    @Mock
    private FastApiRecommendationClient fastApiRecommendationClient;
    @Mock
    private RecommendationPendingFutureRegistry pendingFutureRegistry;
    @Mock
    private CustomerMetrics customerMetrics;
    @Mock
    private Timer.Sample totalSample;
    @Mock
    private Timer.Sample waitSample;

    private final Executor sameThreadExecutor = Runnable::run;
    private SimpleMeterRegistry meterRegistry;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        when(customerMetrics.startSample()).thenReturn(totalSample, waitSample);
        recommendationService = new RecommendationService(
                memberRepository,
                personaRecommendationRepository,
                fastApiRecommendationClient,
                pendingFutureRegistry,
                sameThreadExecutor,
                customerMetrics,
                90L,
                meterRegistry
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
            verify(fastApiRecommendationClient, never()).triggerRecommendation(any());
        }

        @Test
        @DisplayName("캐시 miss 시 Future 등록 후 trigger 호출, 타임아웃 시 PENDING 반환")
        void cacheMiss_triggersFastApi_awaitsFuture_returnsPendingOnTimeout() {
            when(memberRepository.existsById(MEMBER_ID)).thenReturn(true);
            when(personaRecommendationRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());
            CompletableFuture<RecommendationResult> future = new CompletableFuture<>();
            when(pendingFutureRegistry.getOrCreate(MEMBER_ID)).thenReturn(future);

            RecommendationService shortTimeoutService = new RecommendationService(
                    memberRepository,
                    personaRecommendationRepository,
                    fastApiRecommendationClient,
                    pendingFutureRegistry,
                    sameThreadExecutor,
                    customerMetrics,
                    1L,
                    meterRegistry
            );

            RecommendationResult result = shortTimeoutService.getRecommendations(MEMBER_ID);

            assertThat(result.source()).isEqualTo(RecommendationResult.RecommendationSource.PENDING);
            assertThat(result.recommendedProducts()).isEmpty();
            assertThat(result.cachedLlmRecommendation()).contains("생성 중");
            verify(fastApiRecommendationClient).triggerRecommendation(MEMBER_ID);
        }

        @Test
        @DisplayName("캐시 miss 후 Kafka로 Future 완료되면 FASTAPI 소스 반환")
        void cacheMiss_futureCompletedByConsumer_returnsFastApiSource() {
            when(memberRepository.existsById(MEMBER_ID)).thenReturn(true);
            when(personaRecommendationRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());
            PersonaRecommendation saved = validCachedEntity(MEMBER_ID);
            saved.updateRecommendation(PersonaSegment.CHURN_RISK, "추천 문구", List.of(
                    new RecommendedProductItem(1, 2L, null, null, null, null, List.of(), "이유2")));
            CompletableFuture<RecommendationResult> future = new CompletableFuture<>();
            when(pendingFutureRegistry.getOrCreate(MEMBER_ID)).thenReturn(future);
            doAnswer(inv -> {
                future.complete(RecommendationResult.fromEntity(saved, RecommendationResult.RecommendationSource.FASTAPI));
                return Optional.empty();
            }).when(fastApiRecommendationClient).triggerRecommendation(MEMBER_ID);

            RecommendationResult result = recommendationService.getRecommendations(MEMBER_ID);

            assertThat(result.source()).isEqualTo(RecommendationResult.RecommendationSource.FASTAPI);
            assertThat(result.segment()).isEqualTo(PersonaSegment.CHURN_RISK);
            verify(fastApiRecommendationClient).triggerRecommendation(MEMBER_ID);
        }

        @Test
        @DisplayName("회원 없으면 MEMBER_NOT_FOUND")
        void memberNotFound_throws() {
            when(memberRepository.existsById(MEMBER_ID)).thenReturn(false);

            assertThatThrownBy(() -> recommendationService.getRecommendations(MEMBER_ID))
                    .isInstanceOf(CustomerException.class)
                    .extracting(ex -> ((CustomerException) ex).getErrorCode())
                    .isEqualTo(CustomerErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private static PersonaRecommendation validCachedEntity(Long memberId) {
        return PersonaRecommendation.builder()
                .memberId(memberId)
                .segment(PersonaSegment.NORMAL)
                .cachedLlmRecommendation("캐시 문구")
                .recommendedProducts(List.of(new RecommendedProductItem(1, 1L, null, null, null, null, List.of(), "이유")))
                .updatedAt(Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS))
                .build();
    }
}
