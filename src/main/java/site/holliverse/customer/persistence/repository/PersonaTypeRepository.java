package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import site.holliverse.customer.persistence.entity.PersonaType;

import java.util.Optional;

@Profile("customer")
public interface PersonaTypeRepository extends JpaRepository<PersonaType, Long> {

    /**
     * 기본 fallback 페르소나 조회.
     * 우선순위: SPACE_EXPLORER -> 최신 version -> 최신 id
     */
    @Query(value = """
            SELECT *
            FROM persona_type p
            WHERE p.is_active = TRUE
            ORDER BY
                CASE WHEN p.character_name = 'SPACE_EXPLORER' THEN 0 ELSE 1 END,
                p.version DESC,
                p.persona_type_id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<PersonaType> findDefaultFallback();
}
