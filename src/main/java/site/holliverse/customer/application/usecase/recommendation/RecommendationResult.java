package site.holliverse.customer.application.usecase.recommendation;

import site.holliverse.customer.persistence.entity.RecommendedProductItem;
import site.holliverse.shared.domain.model.PersonaSegment;

import java.time.Instant;
import java.util.List;

/**
 * 추천 조회/갱신 API 결과 (캐시 / FastAPI / 생성 중 구분).
 */
public record RecommendationResult(
        PersonaSegment segment,
        String cachedLlmRecommendation,
        List<RecommendedProductItem> recommendedProducts,
        RecommendationSource source,
        Instant updatedAt
) {
    public enum RecommendationSource {
        CACHE,   // DB persona_recommendation에서 조회
        FASTAPI, // FastAPI 호출 후 DB 저장·반환
        PENDING  // 캐시 없음, FastAPI 비동기 호출 중 → 즉시 메시지만 반환
    }

    /** 캐시 미스 시 FastAPI 비동기 호출 후 클라이언트에 즉시 반환할 결과 (상품 없음, 안내 문구만). */
    public static RecommendationResult pending(String message) {
        return new RecommendationResult(
                PersonaSegment.NORMAL,
                message != null && !message.isBlank() ? message : "추천을 생성 중입니다. 잠시 후 다시 조회해 주세요.",
                List.of(),
                RecommendationSource.PENDING,
                java.time.Instant.now()
        );
    }
}
