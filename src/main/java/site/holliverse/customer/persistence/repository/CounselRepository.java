package site.holliverse.customer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.SupportCase;


public interface CounselRepository extends JpaRepository<SupportCase,Long> {
}
