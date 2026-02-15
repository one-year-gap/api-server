package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.persistence.BaseEntity;

/**
 * 태블릿/스마트워치 요금제 상세 (Product 1:1).
 */
@Entity
@Table(name = "tab_watch_plan")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TabWatchPlan extends BaseEntity {

    @Id @Column(name = "product_id")
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    /** 제공 데이터 (예: 1GB 공유 가능) */
    @Column(name = "data_amount", nullable = false, length = 100)
    private String dataAmount;

    @Column(name = "benefit_voice_call", length = 100)
    private String benefitVoiceCall;

    @Column(name = "benefit_sms", length = 100)
    private String benefitSms;
}
