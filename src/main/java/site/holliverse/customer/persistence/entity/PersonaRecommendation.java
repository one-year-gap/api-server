package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import site.holliverse.shared.domain.model.PersonaSegment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 페르소나 기반 추천 캐시 (회원당 1행, 7일 캐시는 updated_at 기준).
 * <p>
 * DB는 member(member_id)와 식별 관계(1:1). JPA에서는 Member 연관 없이 member_id만 FK로 보관.
 * API는 member_id로 조회·갱신하면 되고, Member 엔티티 로딩이 필요 없어 연관을 두지 않음.
 */
@Entity
@Table(name = "persona_recommendation")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaRecommendation {

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "segment", nullable = false, length = 20)
    private PersonaSegment segment;

    @Column(name = "cached_llm_recommendation", nullable = false, columnDefinition = "TEXT")
    private String cachedLlmRecommendation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "recommended_products", nullable = false, columnDefinition = "jsonb")
    private List<RecommendedProductItem> recommendedProducts = new ArrayList<>();

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** 캐시 갱신 시 segment, 문구, 추천 상품 목록만 업데이트. */
    public void updateRecommendation(PersonaSegment segment, String cachedLlmRecommendation,
                                    List<RecommendedProductItem> recommendedProducts) {
        this.segment = segment;
        this.cachedLlmRecommendation = cachedLlmRecommendation;
        this.recommendedProducts = recommendedProducts != null ? new ArrayList<>(recommendedProducts) : new ArrayList<>();
    }
}
