package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.persistence.BaseEntity;

/**
 * 모바일(5G/LTE) 요금제 상세.
 * Product와 1:1, product_id를 PK로 공유(@MapsId).
 */
@Entity
@Table(name = "mobile_plan")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MobilePlan extends BaseEntity {

    @Id @Column(name = "product_id")
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    // 제공 데이터
    @Column(name = "data_amount", nullable = false, length = 100)
    private String dataAmount;

    // 테더링+쉐어링 데이터
    @Column(name = "tethering_sharing_data", length = 100)
    private String tetheringSharingData;

    @Column(name = "benefit_brands", length = 100)
    private String benefitBrands;

    @Column(name = "benefit_voice_call", nullable = false, length = 100)
    private String benefitVoiceCall;

    @Column(name = "benefit_sms", nullable = false, length = 100)
    private String benefitSms;

    @Column(name = "benefit_media", length = 100)
    private String benefitMedia;

    @Column(name = "benefit_premium", length = 100)
    private String benefitPremium;

    @Column(name = "benefit_signature_family_discount", length = 100)
    private String benefitSignatureFamilyDiscount;
}
