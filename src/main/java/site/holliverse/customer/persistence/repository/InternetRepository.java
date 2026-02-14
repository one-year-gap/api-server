package site.holliverse.customer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Internet;

public interface InternetRepository extends JpaRepository<Internet, Long> {
}
