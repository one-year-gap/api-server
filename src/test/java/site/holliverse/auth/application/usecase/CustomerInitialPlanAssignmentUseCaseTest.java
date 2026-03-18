package site.holliverse.auth.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.shared.domain.model.ProductType;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Member;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerInitialPlanAssignmentUseCase unit test")
class CustomerInitialPlanAssignmentUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private CustomerInitialPlanAssignmentUseCase useCase;

    @Test
    // 이미 활성 모바일 요금제가 있으면 추가 생성하지 않음
    @DisplayName("does nothing when member already has active mobile subscription")
    void doesNothingWhenAlreadyAssigned() {
        Member member = member(1L);
        when(subscriptionRepository.findActiveByMemberIdAndProductType(1L, ProductType.MOBILE_PLAN))
                .thenReturn(Optional.of(Subscription.builder().build()));

        useCase.assignForNewMember(member);

        verify(subscriptionRepository).findActiveByMemberIdAndProductType(1L, ProductType.MOBILE_PLAN);
        verify(productRepository, never()).countByProductType(any());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    // 모바일 요금제가 없으면 예외를 던짐
    @DisplayName("throws NOT_FOUND when there is no mobile plan product")
    void throwsWhenNoMobilePlan() {
        Member member = member(2L);
        when(subscriptionRepository.findActiveByMemberIdAndProductType(2L, ProductType.MOBILE_PLAN))
                .thenReturn(Optional.empty());
        when(productRepository.countByProductType(ProductType.MOBILE_PLAN)).thenReturn(0L);

        assertThatThrownBy(() -> useCase.assignForNewMember(member))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException custom = (CustomException) ex;
                    assertThat(custom.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(custom.getField()).isEqualTo("mobilePlan");
                });
    }

    @Test
    // 초기 요금제 할당 시 24개월 약정 정보가 포함된 구독을 생성함
    @DisplayName("assigns one active mobile subscription with 24-month contract")
    void assignsRandomMobilePlan() {
        Member member = member(3L);
        Product plan = mobilePlan(100L);

        when(subscriptionRepository.findActiveByMemberIdAndProductType(3L, ProductType.MOBILE_PLAN))
                .thenReturn(Optional.empty());
        when(productRepository.countByProductType(ProductType.MOBILE_PLAN)).thenReturn(1L);
        when(productRepository.findByProductType(eq(ProductType.MOBILE_PLAN), eq(PageRequest.of(0, 1))))
                .thenReturn(new PageImpl<>(List.of(plan)));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.assignForNewMember(member);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        Subscription saved = captor.getValue();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getProduct()).isEqualTo(plan);
        assertThat(saved.getStatus()).isTrue();
        assertThat(saved.getEndDate()).isNull();
        assertThat(saved.getStartDate()).isNotNull();
        assertThat(saved.getContractMonths()).isEqualTo(24);
        assertThat(saved.getContractEndDate()).isEqualTo(saved.getStartDate().plusMonths(24));
    }

    private Member member(Long id) {
        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Product mobilePlan(Long id) {
        return Product.builder()
                .productId(id)
                .productCode("PLAN_MOB_TEST")
                .name("Test mobile plan")
                .price(10000)
                .salePrice(9000)
                .productType(ProductType.MOBILE_PLAN)
                .discountType("Test discount")
                .build();
    }
}
