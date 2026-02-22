package site.holliverse.customer.application.usecase.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.domain.policy.SubscriptionChangeDecision;
import site.holliverse.customer.domain.policy.SubscriptionChangePolicy;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.repository.MemberRepository;
import site.holliverse.shared.domain.model.ProductType;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Profile("customer")
public class ChangeProductUseCase {

    private final SubscriptionChangePolicy subscriptionChangePolicy;
    private final SubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ChangeProductUseCase(SubscriptionChangePolicy subscriptionChangePolicy,
                                SubscriptionRepository subscriptionRepository,
                                ProductRepository productRepository,
                                MemberRepository memberRepository,
                                Clock clock) {
        this.subscriptionChangePolicy = subscriptionChangePolicy;
        this.subscriptionRepository = subscriptionRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    /**
     * 신규 가입: 해당 타입 활성 구독 없으면 새로 생성.
     * 요금제 변경: 해당 타입 활성 구독 있으면 기존 해지 후 신규 생성.
     * 규칙: 타입별 활성 구독은 회원당 1개만 유지된다.
     */
    @Transactional
    public ChangeProductResult execute(Long memberId, Long targetProductId) {
        var targetProduct = productRepository.findById(targetProductId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "product", "대상 요금제를 찾을 수 없습니다."));

        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "member", "회원 정보를 찾을 수 없습니다."));

        var currentSameType = subscriptionRepository.findActiveByMemberIdAndProductType(memberId, targetProduct.getProductType());
        Long currentProductId = currentSameType.map(s -> s.getProduct().getProductId()).orElse(null);

        SubscriptionChangeDecision decision = subscriptionChangePolicy.decide(currentProductId, targetProduct.getProductId());

        LocalDateTime now = LocalDateTime.now(clock);
        if (decision.deactivateCurrent() && currentSameType.isPresent()) {
            currentSameType.get().deactivate(now);
        }
        var newSubscription = Subscription.createActive(member, targetProduct, now);
        subscriptionRepository.save(newSubscription);
        return ChangeProductResult.from(newSubscription, targetProduct);
    }

    /**
     * 비교 API용: 회원이 현재 구독 중인 모바일 요금제 상품 ID.
     * (비교는 현재 모바일만 지원)
     */
    @Transactional(readOnly = true)
    public Optional<Long> findCurrentMobileProductId(Long memberId) {
        return subscriptionRepository
                .findActiveByMemberIdAndProductType(memberId, ProductType.MOBILE_PLAN)
                .map(s -> s.getProduct().getProductId());
    }
}
