package site.holliverse.auth.application.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.auth.application.port.InitialPlanAssignmentService;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.shared.domain.model.ProductType;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
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
        // 이미 모바일 요금제 활성 구독이 있으면 중복 생성하지 않는다.
        boolean alreadyAssigned = subscriptionRepository
                .findActiveByMemberIdAndProductType(member.getId(), ProductType.MOBILE_PLAN)
                .isPresent();
        if (alreadyAssigned) {
            return;
        }

        long count = productRepository.countByProductType(ProductType.MOBILE_PLAN);
        if (count <= 0) {
            throw new CustomException(ErrorCode.NOT_FOUND, "mobilePlan");
        }

        int randomPage = ThreadLocalRandom.current().nextInt(Math.toIntExact(count));
        Product randomPlan = productRepository
                .findByProductType(ProductType.MOBILE_PLAN, PageRequest.of(randomPage, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "mobilePlan"));

        Subscription subscription = Subscription.createActive(member, randomPlan, LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }
}
