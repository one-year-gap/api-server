package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.shared.domain.model.ProductType;

@Profile("customer")
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByProductType(ProductType productType, Pageable pageable);
}
