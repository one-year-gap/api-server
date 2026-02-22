package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.MobilePlan;

import java.util.List;

@Profile("customer")
public interface MobilePlanRepository extends JpaRepository<MobilePlan, Long> {

    List<MobilePlan> findByProductIdIn(List<Long> productIds);
}
