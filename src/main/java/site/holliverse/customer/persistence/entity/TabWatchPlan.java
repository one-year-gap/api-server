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
 * 태블릿/스마트워치 요금제 상세 (Product 1:1).
 */
@Entity
@Table(name = "tab_watch_plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TabWatchPlan {

    @Id @Column(name = "product_id")
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    /** 제공 데이터 (예: 1GB 공유 가능) */
    @Column(name = "data_amount", length = 100)
    private String dataAmount;

    @Column(name = "benefit_voice_call", length = 100)
    private String benefitVoiceCall;

    @Column(name = "benefit_sms", length = 50)
    private String benefitSms;
}
