package site.holliverse.customer.application.usecase.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.domain.policy.SubscriptionChangePolicy;
import site.holliverse.customer.domain.policy.SubscriptionChangeResult;

@Service
public class ChangeProductUseCase {

    private final SubscriptionChangePolicy subscriptionChangePolicy;

    public ChangeProductUseCase(SubscriptionChangePolicy subscriptionChangePolicy) {
        this.subscriptionChangePolicy = subscriptionChangePolicy;
    }

    @Transactional
    public ChangeProductResult execute(Long memberId, Long targetProductId) {
        SubscriptionChangeResult result = subscriptionChangePolicy.execute(memberId, targetProductId);
        return ChangeProductResult.from(result.newSubscription(), result.product());
    }
}
