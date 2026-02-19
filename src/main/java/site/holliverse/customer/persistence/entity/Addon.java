package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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

    @Id @Column(name = "product_id")
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    /** 부가 서비스 타입 */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "addon_type", nullable = false, length = 30)
    private AddonType addonType;

    /** 부가 서비스 설명 */
    @Column(name = "description", nullable = false, length = 500)
    private String description;
}
