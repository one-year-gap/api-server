package site.holliverse.customer.domain.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class SubscriptionChangePolicyTest {

    @InjectMocks
    private SubscriptionChangePolicy subscriptionChangePolicy;

    private static final Long TARGET_PRODUCT_ID = 5L;
    private static final Long OTHER_PRODUCT_ID = 3L;

    @Nested
    @DisplayName("decide - 구독 변경 의사결정 (엔티티 없이 ID만 사용)")
    class Decide {

        @Test
        @DisplayName("신규 가입: currentProductId null → deactivateCurrent false, createNew true")
        void newSubscription_returnsCreateNewOnly() {
            SubscriptionChangeDecision decision = subscriptionChangePolicy.decide(null, TARGET_PRODUCT_ID);

            assertThat(decision.deactivateCurrent()).isFalse();
            assertThat(decision.createNew()).isTrue();
        }

        @Test
        @DisplayName("요금제 변경: currentProductId 있음 → deactivateCurrent true, createNew true")
        void changePlan_returnsDeactivateAndCreateNew() {
            SubscriptionChangeDecision decision = subscriptionChangePolicy.decide(OTHER_PRODUCT_ID, TARGET_PRODUCT_ID);

            assertThat(decision.deactivateCurrent()).isTrue();
            assertThat(decision.createNew()).isTrue();
        }

        @Test
        @DisplayName("동일 상품: currentProductId equals targetProductId → CONFLICT 예외")
        void sameProduct_throwsConflict() {
            assertThatThrownBy(() -> subscriptionChangePolicy.decide(TARGET_PRODUCT_ID, TARGET_PRODUCT_ID))
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
