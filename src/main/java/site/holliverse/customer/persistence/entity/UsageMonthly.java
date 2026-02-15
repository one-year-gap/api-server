package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import site.holliverse.shared.persistence.BaseEntity;

import java.util.Map;

@Entity
@Table(name = "usage_monthly")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageMonthly extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    /** 사용 년월 (ex: 202602) */
    @Column(name = "yyyymm", nullable = false, length = 6)
    private String yyyymm;

    /**
     * 상세 사용량 데이터 (JSONB).
     * Hibernate 6: @JdbcTypeCode(SqlTypes.JSON)로 Map을 JSONB에 매핑.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "usage_details", nullable = false)
    private Map<String, Object> usageDetails;
}
