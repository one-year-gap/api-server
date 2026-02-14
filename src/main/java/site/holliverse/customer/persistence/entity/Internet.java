package site.holliverse.customer.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인터넷 요금제 상세 (Product 1:1).
 */
@Entity
@Table(name = "internet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Internet {

    @Id
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    /** 인터넷 최대 속도 (Mbps) */
    @Column(name = "speed_mbps", nullable = false)
    private Integer speedMbps;

    /** 추가 혜택 */
    @Column(name = "addon_benefit", nullable = false, columnDefinition = "TEXT")
    private String addonBenefit;
}
