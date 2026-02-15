package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.persistence.BaseEntity;

/**
 * 인터넷 요금제 상세 (Product 1:1).
 */
@Entity
@Table(name = "internet")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Internet extends BaseEntity {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    /** 요금제 요약 */
    @Column(name = "plan_title", nullable = false, length = 100)
    private String planTitle;

    /** 인터넷 속도 표기 (예: 100Mbps) */
    @Column(name = "speed", nullable = false, length = 50)
    private String speed;

    /** 추가 혜택 */
    @Column(name = "addon_benefit", nullable = false, columnDefinition = "TEXT")
    private String addonBenefit;

    /** 제공혜택 */
    @Column(name = "benefits", nullable = false, length = 255)
    private String benefits;
}
