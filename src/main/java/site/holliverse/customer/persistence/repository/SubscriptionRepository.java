package site.holliverse.customer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.shared.domain.model.ProductType;

import java.util.Optional;

/**
 * 구독(subscription) 엔티티 저장소.
 * 신규 가입/요금제 변경 UseCase에서 활성 구독 조회·저장에 사용.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 회원·상품 타입 기준 활성 구독 1건 조회.
     * (회원당 타입별 최대 1개 활성 구독 가정)
     *
     * @param memberId   회원 ID
     * @param productType 상품 타입 (MOBILE_PLAN, INTERNET, IPTV 등)
     * @return 해당 조건의 활성 구독, 없으면 empty
     */
    @Query("SELECT s FROM Subscription s JOIN FETCH s.product WHERE s.member.id = :memberId AND s.status = true AND s.product.productType = :productType")
    Optional<Subscription> findActiveByMemberIdAndProductType(
            @Param("memberId") Long memberId,
            @Param("productType") ProductType productType
    );
}
