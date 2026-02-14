package site.holliverse.customer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Internet;

import java.util.List;

public interface InternetRepository extends JpaRepository<Internet, Long> {

    List<Internet> findByProductIdIn(List<Long> productIds);
}
