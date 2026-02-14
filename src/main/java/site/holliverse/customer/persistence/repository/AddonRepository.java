package site.holliverse.customer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Addon;

public interface AddonRepository extends JpaRepository<Addon, Long> {
}
