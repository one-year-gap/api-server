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

    /**
     * 구독 비활성화 (요금제 변경 시 기존 구독 해지).
     * status를 false로 두고 종료일을 기록한다.
     */
    public void deactivate(LocalDateTime timestamp) {
        this.status = false;
        this.endDate = timestamp;
    }

    /**
     * 신규 활성 구독 생성 (신규 가입 또는 요금제 변경 후 새 구독).
     *
     * @param member  회원
     * @param product 상품(요금제)
     * @param timestamp 구독 시작 시각
     * @return status=true, endDate=null, startDate=timestamp인 구독
     */
    public static Subscription createActive(Member member, Product product, LocalDateTime timestamp) {
        return Subscription.builder()
                .member(member)
                .product(product)
                .startDate(timestamp)
                .endDate(null)
                .status(true)
                .build();
    }
}