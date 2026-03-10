package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 회원별 최신 페르소나 판정 스냅샷(index_persona_snapshot) 엔티티.
 */
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(IndexPersonaSnapshotId.class)
@Table(name = "index_persona_snapshot")
public class IndexPersonaSnapshot {

    @Id
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "persona_type_id", nullable = false)
    private Long personaTypeId;

    @Column(name = "source_index_code", nullable = false, length = 50)
    private String sourceIndexCode;

    @Column(name = "source_tscore", nullable = false)
    private BigDecimal sourceTscore;
}
