package site.holliverse.customer.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.holliverse.shared.domain.model.AddonType;

/**
 * 부가서비스 상세 (Product 1:1).
 */
@Entity
@Table(name = "addon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Addon {

    @Id @Column(name = "product_id")
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    /** 부가 서비스 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "addon_type", nullable = false, length = 30)
    private AddonType addonType;

    /** 부가 서비스 설명 */
    @Column(nullable = false, columnDefinition = "VARCHAR(500)")
    private String description;
}
