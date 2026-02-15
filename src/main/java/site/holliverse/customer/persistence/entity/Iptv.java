package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.persistence.BaseEntity;

/**
 * IPTV 상품 상세 (Product 1:1).
 */
@Entity
@Table(name = "iptv")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Iptv extends BaseEntity {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    /** 요금제 요약 (웹서핑, 문서 작업 등) */
    @Column(name = "plan_title", nullable = false, length = 100)
    private String planTitle;

    /** 채널 수 */
    @Column(name = "channel", nullable = false)
    private Integer channelCount;

    /** 추가 혜택 (웹서핑, 문서 작업 등) */
    @Column(name = "addon_benefit", nullable = false, columnDefinition = "TEXT")
    private String addonBenefit;

    /** 제공혜택 */
    @Column(name = "benefits", nullable = false, length = 100)
    private String benefits;
}
