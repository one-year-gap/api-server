package site.holliverse.customer.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * IPTV 상품 상세 (Product 1:1).
 * 스키마: internet_phone → 테이블명 iptv (상품타입과 일치).
 */
@Entity
@Table(name = "iptv")
@Getter
public class Iptv {

    @Id
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    /** 채널 수 */
    @Column(name = "channel_text", nullable = false)
    private Integer channelText;

    /** 추가 혜택 (웹서핑, 문서 작업 등) */
    @Column(name = "addon_benefit", columnDefinition = "TEXT")
    private String addonBenefit;

}
