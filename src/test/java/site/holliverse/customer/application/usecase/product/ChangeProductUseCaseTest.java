package site.holliverse.customer.application.usecase.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.domain.policy.SubscriptionChangeDecision;
import site.holliverse.customer.domain.policy.SubscriptionChangePolicy;
import site.holliverse.customer.error.CustomerErrorCode;
import site.holliverse.customer.error.CustomerException;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.shared.domain.model.ProductType;
import site.holliverse.shared.persistence.entity.Member;
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
@DisplayName("ChangeProductUseCase unit test")
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

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 2, 18, 12, 0, 0);
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Long MEMBER_ID = 1L;
    private static final Long TARGET_PRODUCT_ID = 5L;
    private static final Long OTHER_PRODUCT_ID = 3L;
    private static final ProductType PRODUCT_TYPE = ProductType.MOBILE_PLAN;

    private static Member createMember(Long id) {
        return Member.builder()
                .id(id)
                .email("test@test.com")
                .name("Tester")
                .joinDate(LocalDate.now())
                .build();
    }

    private static Product createProduct(Long productId, ProductType type) {
        return Product.builder()
                .productId(productId)
                .productCode("CODE-" + productId)
                .name("Product" + productId)
                .price(10000)
                .salePrice(8000)
                .productType(type)
                .discountType("discount")
                .build();
    }

    private static Subscription createSubscription(Long id, Member member, Product product, boolean status) {
        return Subscription.builder()
                .id(id)
                .member(member)
                .product(product)
                .startDate(LocalDateTime.now())
                .contractMonths(null)
                .contractEndDate(null)
                .endDate(null)
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("execute success flow")
    class ProductApplyChangeFlow {

        @Test
        // 새 구독 생성 시 24개월 약정 정보가 함께 저장되는지 검증
        @DisplayName("creates a new active subscription with 24-month contract")
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

            ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
            verify(subscriptionRepository).save(captor.capture());
            Subscription saved = captor.getValue();
            assertThat(saved.getMember()).isEqualTo(member);
            assertThat(saved.getProduct()).isEqualTo(targetProduct);
            assertThat(saved.getStartDate()).isEqualTo(FIXED_NOW);
            assertThat(saved.getContractMonths()).isEqualTo(24);
            assertThat(saved.getContractEndDate()).isEqualTo(FIXED_NOW.plusMonths(24));
            assertThat(result.productId()).isEqualTo(TARGET_PRODUCT_ID);
            assertThat(result.productName()).isEqualTo(targetProduct.getName());
            assertThat(result.salePrice()).isEqualTo(targetProduct.getSalePrice());
            assertThat(result.startDate()).isEqualTo(FIXED_NOW);
        }

        @Test
        // 기존 구독 해지 후 새 구독에 24개월 약정이 적용되는지 검증
        @DisplayName("deactivates current subscription and saves new 24-month contract")
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

            ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
            verify(subscriptionRepository).save(captor.capture());
            Subscription saved = captor.getValue();
            assertThat(currentSub.getStatus()).isFalse();
            assertThat(currentSub.getEndDate()).isEqualTo(FIXED_NOW);
            assertThat(saved.getContractMonths()).isEqualTo(24);
            assertThat(saved.getContractEndDate()).isEqualTo(FIXED_NOW.plusMonths(24));
            assertThat(result.productId()).isEqualTo(TARGET_PRODUCT_ID);
            assertThat(result.startDate()).isEqualTo(FIXED_NOW);
        }
    }

    @Nested
    @DisplayName("execute not found")
    class NotFound {

        @Test
        @DisplayName("throws NOT_FOUND when target product does not exist")
        void productNotFound_throwsNotFound() {
            given(productRepository.findById(TARGET_PRODUCT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> changeProductUseCase.execute(MEMBER_ID, TARGET_PRODUCT_ID))
                    .isInstanceOf(CustomerException.class)
                    .extracting(ex -> ((CustomerException) ex).getErrorCode())
                    .isEqualTo(CustomerErrorCode.PRODUCT_NOT_FOUND);
            verify(productRepository).findById(TARGET_PRODUCT_ID);
        }

        @Test
        @DisplayName("throws NOT_FOUND when member does not exist")
        void memberNotFound_throwsNotFound() {
            Product targetProduct = createProduct(TARGET_PRODUCT_ID, PRODUCT_TYPE);
            given(productRepository.findById(TARGET_PRODUCT_ID)).willReturn(Optional.of(targetProduct));
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> changeProductUseCase.execute(MEMBER_ID, TARGET_PRODUCT_ID))
                    .isInstanceOf(CustomerException.class)
                    .extracting(ex -> ((CustomerException) ex).getErrorCode())
                    .isEqualTo(CustomerErrorCode.MEMBER_NOT_FOUND);
            verify(memberRepository).findById(MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("execute policy violation")
    class PolicyViolation {

        @Test
        @DisplayName("propagates CONFLICT and does not save when policy rejects same product")
        void whenPolicyThrowsConflict_exceptionPropagatesAndSaveNotCalled() {
            Product targetProduct = createProduct(TARGET_PRODUCT_ID, PRODUCT_TYPE);
            Subscription currentSub = createSubscription(10L, createMember(MEMBER_ID), targetProduct, true);
            given(productRepository.findById(TARGET_PRODUCT_ID)).willReturn(Optional.of(targetProduct));
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(createMember(MEMBER_ID)));
            given(subscriptionRepository.findActiveByMemberIdAndProductType(MEMBER_ID, PRODUCT_TYPE))
                    .willReturn(Optional.of(currentSub));
            given(subscriptionChangePolicy.decide(eq(TARGET_PRODUCT_ID), eq(TARGET_PRODUCT_ID)))
                    .willThrow(new CustomerException(CustomerErrorCode.MOBILE_PLAN_ALREADY_SUBSCRIBED));

            assertThatThrownBy(() -> changeProductUseCase.execute(MEMBER_ID, TARGET_PRODUCT_ID))
                    .isInstanceOf(CustomerException.class)
                    .extracting(ex -> ((CustomerException) ex).getErrorCode())
                    .isEqualTo(CustomerErrorCode.MOBILE_PLAN_ALREADY_SUBSCRIBED);

            verify(subscriptionRepository, never()).save(any(Subscription.class));
        }
    }
}
