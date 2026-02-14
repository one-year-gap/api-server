package site.holliverse.customer.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "product")
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer price;

    /** 할인 적용가. IPTV 등 일부 상품은 미제공 시 null */
    @Column(name = "saled_price")
    private Integer saledPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType productType;

    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    /** 할인 유형 설명 (예: 선택약정 25%, 3년 약정 결합). API discount_type */
    @Column(name = "discount_type", length = 100)
    private String discountType;
}
