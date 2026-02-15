package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.domain.model.ProductType;
import site.holliverse.shared.persistence.BaseEntity;

@Entity
@Table(name = "product")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    /** 할인 후 금액 (상품가격과 동일 가능). 스키마 NOT NULL */
    @Column(name = "saled_price", nullable = false)
    private Integer saledPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType productType;

    /** 할인 유형 설명 (예: 선택약정 25%, 3년 약정 결합). API discount_type */
    @Column(name = "discount_type", length = 100)
    private String discountType;
}
