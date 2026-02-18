package site.holliverse.customer.domain.policy;

import org.springframework.stereotype.Component;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.MemberRepository;

/**
 * 구독 변경 정책. 신규 가입·요금제 변경을 하나의 흐름으로 처리한다.
 */
@Component
public class SubscriptionChangePolicy {

    private final SubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public SubscriptionChangePolicy(SubscriptionRepository subscriptionRepository,
                                   ProductRepository productRepository,
                                   MemberRepository memberRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
    }

    public SubscriptionChangeResult execute(Long memberId, Long targetProductId) {
        // 1. 대상 상품·회원 조회
        Product targetProduct = productRepository.findById(targetProductId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "product", "대상 요금제를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "member", "회원 정보를 찾을 수 없습니다."));

        // 2. 같은 상품 타입의 활성 구독 조회
        var currentOpt = subscriptionRepository.findActiveByMemberIdAndProductType(memberId, targetProduct.getProductType());

        // 3. 있으면 요금제 변경: 동일 상품이면 예외, 아니면 기존 구독 해지
        if (currentOpt.isPresent()) {
            Subscription current = currentOpt.get();
            if (current.getProduct().getProductId().equals(targetProductId)) {
                throw new CustomException(ErrorCode.CONFLICT, "target_product_id", "지금 가입되어있는 상품입니다.");
            }
            current.deactivate();
        }

        // 4. 신규 구독 생성·저장
        Subscription newSub = Subscription.createActive(member, targetProduct);
        subscriptionRepository.save(newSub);

        return new SubscriptionChangeResult(newSub, targetProduct);
    }
}
