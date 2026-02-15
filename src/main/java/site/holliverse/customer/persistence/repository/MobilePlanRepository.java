package site.holliverse.customer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.MobilePlan;

import java.util.List;

public interface MobilePlanRepository extends JpaRepository<MobilePlan, Long> {

    List<MobilePlan> findByProductIdIn(List<Long> productIds);
}
