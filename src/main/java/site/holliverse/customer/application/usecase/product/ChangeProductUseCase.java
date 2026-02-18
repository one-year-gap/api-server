package site.holliverse.customer.application.usecase.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.domain.policy.SubscriptionChangePolicy;
import site.holliverse.customer.domain.policy.SubscriptionChangeResult;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.repository.MemberRepository;

@Service
public class ChangeProductUseCase {

    private final SubscriptionChangePolicy subscriptionChangePolicy;
    private final SubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public ChangeProductUseCase(SubscriptionChangePolicy subscriptionChangePolicy,
                                SubscriptionRepository subscriptionRepository,
                                ProductRepository productRepository,
                                MemberRepository memberRepository) {
        this.subscriptionChangePolicy = subscriptionChangePolicy;
        this.subscriptionRepository = subscriptionRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
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

        SubscriptionChangeResult result = subscriptionChangePolicy.execute(member, targetProduct, currentSameType);

        subscriptionRepository.save(result.newSubscription());
        return ChangeProductResult.from(result.newSubscription(), result.product());
    }
}
