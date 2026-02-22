package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Iptv;

import java.util.List;

@Profile("customer")
public interface IptvRepository extends JpaRepository<Iptv, Long> {

    List<Iptv> findByProductIdIn(List<Long> productIds);
}
