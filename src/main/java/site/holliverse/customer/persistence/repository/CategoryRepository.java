package site.holliverse.customer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, String> {
}
