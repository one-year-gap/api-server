package site.holliverse.customer.persistence.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.holliverse.customer.persistence.entity.ProductViewHistory;
import site.holliverse.customer.persistence.entity.ProductViewHistoryId;

import java.util.List;

@Profile("customer")
public interface ProductViewHistoryRepository extends JpaRepository<ProductViewHistory, ProductViewHistoryId> {

    @Query("""
        SELECT p FROM ProductViewHistory p
        WHERE p.memberId = :memberId
        ORDER BY p.viewedAt DESC
        """)
    List<ProductViewHistory> findRecentByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable
    );
}

