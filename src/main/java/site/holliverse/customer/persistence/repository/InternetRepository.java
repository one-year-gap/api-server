package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Internet;

import java.util.List;

@Profile("customer")
public interface InternetRepository extends JpaRepository<Internet, Long> {

    List<Internet> findByProductIdIn(List<Long> productIds);
}
