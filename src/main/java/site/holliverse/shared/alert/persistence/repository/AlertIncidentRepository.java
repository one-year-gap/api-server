package site.holliverse.shared.alert.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.shared.alert.persistence.entity.AlertIncident;

import java.util.Optional;

public interface AlertIncidentRepository extends JpaRepository<AlertIncident, Long> {
    Optional<AlertIncident> findByFingerprint(String fingerprint);
}
