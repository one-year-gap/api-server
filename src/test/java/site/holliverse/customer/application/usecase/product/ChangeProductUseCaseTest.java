package site.holliverse.customer.application.usecase.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.domain.policy.SubscriptionChangeDecision;
import site.holliverse.customer.domain.policy.SubscriptionChangePolicy;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.domain.model.ProductType;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeProductUseCase 단위 테스트")
class ChangeProductUseCaseTest {

    @Mock
    private SubscriptionChangePolicy subscriptionChangePolicy;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private Clock clock;

    @InjectMocks
    private ChangeProductUseCase changeProductUseCase;

    /** 테스트에서 사용할 고정 시각 (deactivate/createActive 일관성·검증용) */
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 2, 18, 12, 0, 0);
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private static final Long MEMBER_ID = 1L;
    private static final Long TARGET_PRODUCT_ID = 5L;
    private static final Long OTHER_PRODUCT_ID = 3L;
    private static final ProductType PRODUCT_TYPE = ProductType.MOBILE_PLAN;

    // -- Helper  methods --
    private static Member createMember(Long id) {
        return Member.builder()
                .id(id)
                .email("test@test.com")
                .name("테스트회원")
                .joinDate(LocalDate.now())
                .build();
    }

    private static Product createProduct(Long productId, ProductType type) {
        return Product.builder()
                .productId(productId)
                .productCode("CODE-" + productId)
                .name("상품" + productId)
                .price(10000)
                .salePrice(8000)
                .productType(type)
                .discountType("할인")
                .build();
    }

    private static Subscription createSubscription(Long id, Member member, Product product, boolean status) {
        return Subscription.builder()
                .id(id)
                .member(member)
                .product(product)
                .startDate(LocalDateTime.now())
                .endDate(null)
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("execute - 상품 신청 / 변경 흐름 검증")
    class ProductApplyChangeFlow {

        @Test
        @DisplayName("성공(신규): 정책 decide 후 새 구독 생성·저장 후 결과 반환")
        void success_orchestration() {
            given(clock.instant()).willReturn(FIXED_NOW.atZone(ZONE).toInstant());
            given(clock.getZone()).willReturn(ZONE);
            Member member = createMember(MEMBER_ID);
            Product targetProduct = createProduct(TARGET_PRODUCT_ID, PRODUCT_TYPE);
            given(productRepository.findById(TARGET_PRODUCT_ID)).willReturn(Optional.of(targetProduct));
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(subscriptionRepository.findActiveByMemberIdAndProductType(MEMBER_ID, PRODUCT_TYPE))
                    .willReturn(Optional.empty());
            given(subscriptionChangePolicy.decide(eq(null), eq(TARGET_PRODUCT_ID)))
                    .willReturn(new SubscriptionChangeDecision(false, true));
            given(subscriptionRepository.save(any(Subscription.class))).willAnswer(inv -> inv.getArgument(0));

            ChangeProductResult result = changeProductUseCase.execute(MEMBER_ID, TARGET_PRODUCT_ID);

            verify(subscriptionRepository).save(any(Subscription.class));
            assertThat(result.productId()).isEqualTo(TARGET_PRODUCT_ID);
            assertThat(result.productName()).isEqualTo(targetProduct.getName());
            assertThat(result.salePrice()).isEqualTo(targetProduct.getSalePrice());
            assertThat(result.startDate()).isEqualTo(FIXED_NOW);
        }

        @Test
        @DisplayName("성공(변경): 기존 구독 해지 후 새 구독 저장, 동일 시각 적용")
        void changePlan_deactivatesCurrentAndSavesNew() {
            given(clock.instant()).willReturn(FIXED_NOW.atZone(ZONE).toInstant());
            given(clock.getZone()).willReturn(ZONE);
            Member member = createMember(MEMBER_ID);
            Product targetProduct = createProduct(TARGET_PRODUCT_ID, PRODUCT_TYPE);
            Product currentProduct = createProduct(OTHER_PRODUCT_ID, PRODUCT_TYPE);
            Subscription currentSub = createSubscription(10L, member, currentProduct, true);
            given(productRepository.findById(TARGET_PRODUCT_ID)).willReturn(Optional.of(targetProduct));
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(subscriptionRepository.findActiveByMemberIdAndProductType(MEMBER_ID, PRODUCT_TYPE))
                    .willReturn(Optional.of(currentSub));
            given(subscriptionChangePolicy.decide(eq(OTHER_PRODUCT_ID), eq(TARGET_PRODUCT_ID)))
                    .willReturn(new SubscriptionChangeDecision(true, true));
            given(subscriptionRepository.save(any(Subscription.class))).willAnswer(inv -> inv.getArgument(0));

            ChangeProductResult result = changeProductUseCase.execute(MEMBER_ID, TARGET_PRODUCT_ID);

            assertThat(currentSub.getStatus()).isFalse();
            assertThat(currentSub.getEndDate()).isEqualTo(FIXED_NOW);
            verify(subscriptionRepository).save(any(Subscription.class));
            assertThat(result.productId()).isEqualTo(TARGET_PRODUCT_ID);
            assertThat(result.startDate()).isEqualTo(FIXED_NOW);
        }
    }

    @Nested
    @DisplayName("execute - 조회 실패")
    class NotFound {

        @Test
        @DisplayName("대상 상품이 없으면 NOT_FOUND 예외")
        void productNotFound_throwsNotFound() {
            given(productRepository.findById(TARGET_PRODUCT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> changeProductUseCase.execute(MEMBER_ID, TARGET_PRODUCT_ID))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                        assertThat(ce.getField()).isEqualTo("product");
                        assertThat(ce.getReason()).isEqualTo("대상 요금제를 찾을 수 없습니다.");
                    });
            verify(productRepository).findById(TARGET_PRODUCT_ID);
        }

        @Test
        @DisplayName("회원이 없으면 NOT_FOUND 예외")
        void memberNotFound_throwsNotFound() {
            Product targetProduct = createProduct(TARGET_PRODUCT_ID, PRODUCT_TYPE);
            given(productRepository.findById(TARGET_PRODUCT_ID)).willReturn(Optional.of(targetProduct));
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> changeProductUseCase.execute(MEMBER_ID, TARGET_PRODUCT_ID))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                        assertThat(ce.getField()).isEqualTo("member");
                        assertThat(ce.getReason()).isEqualTo("회원 정보를 찾을 수 없습니다.");
                    });
            verify(memberRepository).findById(MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("execute - 정책 규칙 위반")
    class PolicyViolation {

        @Test
        @DisplayName("정책이 CONFLICT(동일 상품) throw 시 예외 전파, save 미호출")
        void whenPolicyThrowsConflict_exceptionPropagatesAndSaveNotCalled() {
            Product targetProduct = createProduct(TARGET_PRODUCT_ID, PRODUCT_TYPE);
            Subscription currentSub = createSubscription(10L, createMember(MEMBER_ID), targetProduct, true);
            given(productRepository.findById(TARGET_PRODUCT_ID)).willReturn(Optional.of(targetProduct));
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(createMember(MEMBER_ID)));
            given(subscriptionRepository.findActiveByMemberIdAndProductType(MEMBER_ID, PRODUCT_TYPE))
                    .willReturn(Optional.of(currentSub));
            given(subscriptionChangePolicy.decide(eq(TARGET_PRODUCT_ID), eq(TARGET_PRODUCT_ID)))
                    .willThrow(new CustomException(ErrorCode.CONFLICT, "target_product_id", "지금 가입되어있는 상품입니다."));

            assertThatThrownBy(() -> changeProductUseCase.execute(MEMBER_ID, TARGET_PRODUCT_ID))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

            verify(subscriptionRepository, never()).save(any(Subscription.class));
        }
    }
}
