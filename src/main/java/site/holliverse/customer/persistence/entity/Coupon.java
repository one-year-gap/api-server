package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.persistence.BaseEntity;
import site.holliverse.shared.domain.model.CouponType;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupon")
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false)
    private CouponType couponType;

    /** 혜택 값 (예: '10%', '10000', '2GB') */
    @Column(name = "benefit_value", nullable = false, length = 50)
    private String benefitValue;

    /** 쿠폰 상세설명 */
    @Column(name = "description", nullable = false, length = 100)
    private String description;

    /** 유효기간 종료일 (고정 기간 정책용) */
    @Column(name = "valid_end_date")
    private LocalDateTime validEndDate;

    /** 유효 기간 (발급 후 유지 일수, 동적 기간 정책용) */
    @Column(name = "valid_days")
    private Integer validDays;
}
