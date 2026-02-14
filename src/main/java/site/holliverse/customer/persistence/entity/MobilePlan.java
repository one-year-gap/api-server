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
 * 모바일(5G/LTE) 요금제 상세.
 * Product와 1:1, product_id를 PK로 공유(@MapsId).
 */
@Entity
@Table(name = "mobile_plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MobilePlan {

    @Id
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    /** 제공 데이터 (목록 dataSummary) */
    @Column(name = "data_amount", nullable = false, length = 50)
    private String dataAmount;

    /** 테더링+쉐어링 데이터 (GB). 스키마 컬럼명 ththering_sharing_data */
    @Column(name = "ththering_sharing_data")
    private Integer tetheringSharingData;

    @Column(name = "benefit_brands", length = 50)
    private String benefitBrands;

    @Column(name = "benefit_voice_call", nullable = false, length = 50)
    private String benefitVoiceCall;

    @Column(name = "benefit_sms", nullable = false, length = 50)
    private String benefitSms;

    @Column(name = "benefit_media", length = 50)
    private String benefitMedia;

    @Column(name = "benefit_premium", length = 50)
    private String benefitPremium;

    @Column(name = "benefit_signature_family_discount")
    private String benefitSignatureFamilyDiscount;
}
