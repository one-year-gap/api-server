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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder.Default
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "contract_months")
    private Integer contractMonths;

    @Column(name = "contract_end_date")
    private LocalDateTime contractEndDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private Boolean status = true;

    public void deactivate(LocalDateTime timestamp) {
        this.status = false;
        this.endDate = timestamp;
    }

    public static Subscription createActive(Member member, Product product, LocalDateTime timestamp) {
        return Subscription.builder()
                .member(member)
                .product(product)
                .startDate(timestamp)
                .contractMonths(null)
                .contractEndDate(null)
                .endDate(null)
                .status(true)
                .build();
    }
}
