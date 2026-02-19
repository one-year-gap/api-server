package site.holliverse.customer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Addon;

import java.util.List;

public interface AddonRepository extends JpaRepository<Addon, Long> {

    List<Addon> findByProductIdIn(List<Long> productIds);
}
