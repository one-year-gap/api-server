package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.customer.persistence.entity.PersonaRecommendation;

@Profile("customer")
public interface PersonaRecommendationRepository extends JpaRepository<PersonaRecommendation, Long> {
}
