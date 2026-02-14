package site.holliverse.customer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Iptv;

public interface IptvRepository extends JpaRepository<Iptv, Long> {
}
