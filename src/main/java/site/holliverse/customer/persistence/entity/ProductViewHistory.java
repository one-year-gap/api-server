package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.persistence.BaseEntity;
import site.holliverse.shared.persistence.entity.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_view_history")
public class ProductViewHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_id")
    private Long id;

    // 연관 관계 (N:1)
    // 어떤 회원이 보았는가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 어떤 상품을 보았는가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder.Default
    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();
}
