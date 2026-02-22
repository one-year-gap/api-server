package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Addon;

import java.util.List;

@Profile("customer")
public interface AddonRepository extends JpaRepository<Addon, Long> {

    List<Addon> findByProductIdIn(List<Long> productIds);
}
