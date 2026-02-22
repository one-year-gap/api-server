package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.TabWatchPlan;

import java.util.List;

@Profile("customer")
public interface TabWatchPlanRepository extends JpaRepository<TabWatchPlan, Long> {

    List<TabWatchPlan> findByProductIdIn(List<Long> productIds);
}
