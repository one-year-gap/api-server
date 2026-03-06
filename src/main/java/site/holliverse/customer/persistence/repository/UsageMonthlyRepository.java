package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.UsageMonthly;

import java.util.Optional;

@Profile("customer")
public interface UsageMonthlyRepository extends JpaRepository<UsageMonthly, Long> {

    Optional<UsageMonthly> findFirstBySubscription_IdOrderByYyyymmDesc(Long subscriptionId);
}
