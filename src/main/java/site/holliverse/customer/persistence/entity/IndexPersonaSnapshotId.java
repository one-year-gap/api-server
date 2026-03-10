package site.holliverse.customer.persistence.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * index_persona_snapshot 복합 PK(snapshot_date, member_id) 키 클래스.
 */
public class IndexPersonaSnapshotId implements Serializable {

    private LocalDate snapshotDate;
    private Long memberId;

    public IndexPersonaSnapshotId() {
    }

    public IndexPersonaSnapshotId(LocalDate snapshotDate, Long memberId) {
        this.snapshotDate = snapshotDate;
        this.memberId = memberId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexPersonaSnapshotId that)) return false;
        return Objects.equals(snapshotDate, that.snapshotDate) && Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshotDate, memberId);
    }
}
