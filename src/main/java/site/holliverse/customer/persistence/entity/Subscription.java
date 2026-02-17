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
@Table(name = "subscription")
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    // 연관 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder.Default
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "end_date")
    private LocalDateTime endDate; // 해지 전까지는 null 허용

    /** 구독 상태 (TRUE: 구독중, FALSE: 해지됨) */
    @Builder.Default
    @Column(name = "status", nullable = false)
    private Boolean status = true;
}
