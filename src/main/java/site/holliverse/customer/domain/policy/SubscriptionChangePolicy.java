package site.holliverse.customer.domain.policy;

import org.springframework.stereotype.Component;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Member;

import java.util.Optional;

@Component
public class SubscriptionChangePolicy {

    public SubscriptionChangeResult execute(
            Member member,
            Product targetProduct,
            Optional<Subscription> currentSameType) {

        if (currentSameType.isPresent()) {
            Subscription current = currentSameType.get();
            if (current.getProduct().getProductId().equals(targetProduct.getProductId())) {
                throw new CustomException(ErrorCode.CONFLICT, "target_product_id", "지금 가입되어있는 상품입니다.");
            }
            current.deactivate();
        }

        Subscription newSub = Subscription.createActive(member, targetProduct);
        return new SubscriptionChangeResult(newSub, targetProduct);
    }
}
