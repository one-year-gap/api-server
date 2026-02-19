package site.holliverse.shared.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.shared.persistence.entity.Address;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address,Long> {
    Optional<Address> findByProvinceAndCityAndStreetAddress(String province, String city, String streetAddress);
}
