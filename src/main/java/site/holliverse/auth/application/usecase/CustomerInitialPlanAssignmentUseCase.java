package site.holliverse.auth.application.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.auth.application.port.InitialPlanAssignmentService;
import site.holliverse.auth.error.AuthErrorCode;
import site.holliverse.auth.error.AuthException;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.shared.domain.model.ProductType;
import site.holliverse.shared.persistence.entity.Member;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Profile("customer")
public class CustomerInitialPlanAssignmentUseCase implements InitialPlanAssignmentService {

    private final ProductRepository productRepository;
    private final SubscriptionRepository subscriptionRepository;

    public CustomerInitialPlanAssignmentUseCase(
            ProductRepository productRepository,
            SubscriptionRepository subscriptionRepository
    ) {
        this.productRepository = productRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional
    public void assignForNewMember(Member member) {
        boolean alreadyAssigned = subscriptionRepository
                .findActiveByMemberIdAndProductType(member.getId(), ProductType.MOBILE_PLAN)
                .isPresent();
        if (alreadyAssigned) {
            return;
        }

        // product_id 1~10 범위의 모바일 요금제 중 하나를 랜덤으로 선택
        long randomProductId = ThreadLocalRandom.current().nextLong(1, 11);
        Product randomPlan = productRepository.findById(randomProductId)
                .filter(product -> product.getProductType() == ProductType.MOBILE_PLAN)
                .orElseThrow(() -> new AuthException(AuthErrorCode.MOBILE_PLAN_NOT_FOUND));

        Subscription subscription = Subscription.createActive(member, randomPlan, LocalDateTime.now(), 24);
        subscriptionRepository.save(subscription);
    }
}
