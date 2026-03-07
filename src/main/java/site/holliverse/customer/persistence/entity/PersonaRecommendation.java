package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import site.holliverse.shared.domain.model.PersonaSegment;
import site.holliverse.shared.persistence.entity.Member;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 페르소나 기반 추천 캐시 (회원당 1행, 7일 캐시는 updated_at 기준).
 * member와 식별 관계(1:1, PK = member_id).
 */
@Entity
@Table(name = "persona_recommendation")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaRecommendation {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

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
}
