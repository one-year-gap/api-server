package site.holliverse.customer.domain.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.domain.model.ProductType;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class SubscriptionChangePolicyTest {

    @InjectMocks
    private SubscriptionChangePolicy subscriptionChangePolicy;

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

    @Nested
    @DisplayName("SubscriptionChangePolicy.execute - 구독 변경 정책 테스트")
    class Execute {

        @Test
        @DisplayName("1. 신규 가입: currentSameType 없음 → 새 구독 생성 후 Result 반환")
        void newSubscription_success() {

            //given: 회원과 상품 준비 -> 기존 구독 없음 
            Member member = createMember(MEMBER_ID);
            Product targetProduct = createProduct(TARGET_PRODUCT_ID, PRODUCT_TYPE);
            Optional<Subscription> currentSameType = Optional.empty();

            //when: 정책 실행
            SubscriptionChangeResult result = subscriptionChangePolicy.execute(member, targetProduct, currentSameType);

            //then: 결과 검증

            assertThat(result.newSubscription().getMember()).isEqualTo(member);
            assertThat(result.newSubscription().getProduct()).isEqualTo(targetProduct);
            assertThat(result.newSubscription().getStatus()).isTrue();
            assertThat(result.newSubscription().getEndDate()).isNull();
            assertThat(result.product()).isEqualTo(targetProduct);
        }

        @Test
        @DisplayName("2. 요금제 변경: 같은 타입 활성 구독 있음 → 기존 해지 후 신규 구독 Result 반환")
        void changePlan_success() {
            // [Given] 기존 구독 상품과 타겟 상품이 동일함 (ID: 101)
            Member member = createMember(1L);

            Product targetProduct = createProduct(101L, ProductType.MOBILE_PLAN);

            Subscription currentSub = Subscription.createActive(member, targetProduct);

            Optional<Subscription> currentSameType = Optional.of(currentSub);

            // [When & Then] 정책 실행 시 예외 발생 확인
            assertThatThrownBy(() -> subscriptionChangePolicy.execute(member, targetProduct, currentSameType))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
                        assertThat(ce.getReason()).contains("지금 가입되어있는 상품입니다.");
                    });
        }

        @Test
        @DisplayName("실패: 현재 가입된 상품과  동일 상품 이미 가입 → CONFLICT 예외가 발생")
        void sameProduct_alreadySubscribed_throwsConflict() {
            
            // given: 기존 구독과 같은 상품
            Member member = createMember(MEMBER_ID);
            Product targetProduct = createProduct(TARGET_PRODUCT_ID, PRODUCT_TYPE);
            Subscription currentSub = Subscription.builder()
                    .id(10L)
                    .member(member)
                    .product(targetProduct)
                    .status(true)
                    .endDate(null)
                    .build();

            assertThatThrownBy(() -> subscriptionChangePolicy.execute(
                    member, targetProduct, Optional.of(currentSub)))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
                        assertThat(ce.getField()).isEqualTo("target_product_id");
                        assertThat(ce.getReason()).isEqualTo("지금 가입되어있는 상품입니다.");
                    });
        }

    }
}
