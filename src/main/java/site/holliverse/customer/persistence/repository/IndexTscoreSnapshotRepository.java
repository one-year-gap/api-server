package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.IndexTscoreSnapshot;
import site.holliverse.customer.persistence.entity.IndexTscoreSnapshotId;

import java.util.Optional;

@Profile("customer")
public interface IndexTscoreSnapshotRepository extends JpaRepository<IndexTscoreSnapshot, IndexTscoreSnapshotId> {

    /** 회원 기준 최신 T-score 스냅샷 1건 조회. */
    Optional<IndexTscoreSnapshot> findTopByMemberIdOrderBySnapshotDateDesc(Long memberId);
}
