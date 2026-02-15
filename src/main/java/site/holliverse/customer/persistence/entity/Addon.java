package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.persistence.BaseEntity;
import site.holliverse.shared.domain.model.AddonType;

/**
 * 부가서비스 상세 (Product 1:1).
 */
@Entity
@Table(name = "addon_service")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Addon extends BaseEntity {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "addon_type", nullable = false, length = 30)
    private AddonType addonType;

    /** 부가 서비스 설명 */
    @Column(name = "description", nullable = false, length = 500)
    private String description;
}
