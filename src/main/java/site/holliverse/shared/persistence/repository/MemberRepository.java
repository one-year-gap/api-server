package site.holliverse.shared.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.holliverse.shared.persistence.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<Member> findByEmail(String email);
}
