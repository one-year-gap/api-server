package site.holliverse.customer.application.usecase.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.domain.policy.SubscriptionChangePolicy;
import site.holliverse.customer.domain.policy.SubscriptionChangeResult;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.domain.model.ProductType;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
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

    @InjectMocks
    private ChangeProductUseCase changeProductUseCase;

    private static final Long MEMBER_ID = 1L;
    private static final Long TARGET_PRODUCT_ID = 5L;
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
        @DisplayName("성공: 정책 결과로 받은 새로운 구독을 저장하고 결과 반환")
        void success_orchestration() {
            // given
            Member member = createMember(MEMBER_ID);
            Product targetProduct = createProduct(TARGET_PRODUCT_ID, PRODUCT_TYPE);
            Subscription newSub = createSubscription(100L, member, targetProduct, true);
            given(productRepository.findById(TARGET_PRODUCT_ID)).willReturn(Optional.of(targetProduct));
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(subscriptionRepository.findActiveByMemberIdAndProductType(MEMBER_ID, PRODUCT_TYPE))
                    .willReturn(Optional.empty());
            given(subscriptionChangePolicy.execute(member, targetProduct, Optional.empty()))
                    .willReturn(new SubscriptionChangeResult(newSub, targetProduct));

            // when
            ChangeProductResult result = changeProductUseCase.execute(MEMBER_ID, TARGET_PRODUCT_ID);

            // then
            verify(subscriptionRepository).save(newSub); // 새로운 구독 저장
            // 반환값 검증
            assertThat(result.productId()).isEqualTo(TARGET_PRODUCT_ID);
            assertThat(result.productName()).isEqualTo(targetProduct.getName());
            assertThat(result.salePrice()).isEqualTo(targetProduct.getSalePrice());
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
}
