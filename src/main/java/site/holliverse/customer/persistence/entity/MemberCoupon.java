package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_coupon")
public class MemberCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_coupon_id")
    private Long id;

    // 연관 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    // 사용 여부
    @Builder.Default
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    // 사용 날짜
    @Column(name = "used_at")
    private LocalDateTime usedAt; // 사용 전까지는 NULL

    // 발급 받은 날짜
    @Builder.Default
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    // 만료 날짜
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt; // 발급 시점에 계산하여 저장 필수
}
