package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 회원별 T-score 스냅샷(index_tscore_snapshot) 엔티티.
 */
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(IndexTscoreSnapshotId.class)
@Table(name = "index_tscore_snapshot")
public class IndexTscoreSnapshot {

    @Id
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "explore_tscore", nullable = false)
    private BigDecimal exploreTscore;

    @Column(name = "benefit_trend_tscore", nullable = false)
    private BigDecimal benefitTrendTscore;

    @Column(name = "multi_device_tscore", nullable = false)
    private BigDecimal multiDeviceTscore;

    @Column(name = "family_home_tscore", nullable = false)
    private BigDecimal familyHomeTscore;

    @Column(name = "internet_security_tscore", nullable = false)
    private BigDecimal internetSecurityTscore;

    @Column(name = "stability_tscore", nullable = false)
    private BigDecimal stabilityTscore;
}
