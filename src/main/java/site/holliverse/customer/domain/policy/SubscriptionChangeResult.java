package site.holliverse.customer.domain.policy;

import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;

/**
 * 구독 변경 정책 실행 결과. Application 계층에서 ChangeProductResult 등으로 변환해 사용한다.
 */
public record SubscriptionChangeResult(
        Subscription newSubscription,
        Product product
) {}
