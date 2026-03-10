package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.IndexPersonaSnapshot;
import site.holliverse.customer.persistence.entity.IndexPersonaSnapshotId;

import java.util.Optional;

@Profile("customer")
public interface IndexPersonaSnapshotRepository extends JpaRepository<IndexPersonaSnapshot, IndexPersonaSnapshotId> {

    /** 회원 기준 최신 페르소나 스냅샷 1건 조회. */
    Optional<IndexPersonaSnapshot> findTopByMemberIdOrderBySnapshotDateDesc(Long memberId);
}
