package site.holliverse.customer.domain.policy;

import org.springframework.stereotype.Component;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.util.Objects;

/**
 * 구독 변경 규칙만 담당. 엔티티 조작은 하지 않으며, 의사결정 결과만 반환한다.
 */
@Component
public class SubscriptionChangePolicy {

    /**
     * 현재 구독 상품 ID와 대상 상품 ID로 변경 가능 여부 및 수행할 액션을 결정한다.
     *
     * @param currentProductId 같은 타입 활성 구독의 상품 ID (없으면 null)
     * @param targetProductId  변경/신규 가입 대상 상품 ID
     * @return deactivateCurrent: 기존 구독 해지 여부, createNew: 신규 구독 생성 여부
     * @throws CustomException CONFLICT when currentProductId equals targetProductId (동일 상품 재가입)
     */
    public SubscriptionChangeDecision decide(Long currentProductId, Long targetProductId) {
        if (Objects.equals(currentProductId, targetProductId)) {
            throw new CustomException(ErrorCode.CONFLICT, "target_product_id", "지금 가입되어있는 상품입니다.");
        }
        boolean deactivateCurrent = currentProductId != null;
        return new SubscriptionChangeDecision(deactivateCurrent, true);
    }
}
