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
// Customer 초기 요금제 자동 할당 유스케이스 단위 테스트
class CustomerInitialPlanAssignmentUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private CustomerInitialPlanAssignmentUseCase useCase;

    @Test
    @DisplayName("이미 모바일 활성 구독이 있으면 새로 생성하지 않는다")
    void doesNothingWhenAlreadyAssigned() {
        // given: 회원에게 이미 모바일 활성 구독이 있는 상황
        Member member = member(1L);
        when(subscriptionRepository.findActiveByMemberIdAndProductType(1L, ProductType.MOBILE_PLAN))
                .thenReturn(Optional.of(Subscription.builder().build()));

        // when: 초기 요금제 자동 할당 실행
        useCase.assignForNewMember(member);

        // then: 후보 조회/저장을 수행하지 않고 종료한다.
        verify(subscriptionRepository).findActiveByMemberIdAndProductType(1L, ProductType.MOBILE_PLAN);
        verify(productRepository, never()).countByProductType(any());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("모바일 요금제 후보가 없으면 NOT_FOUND 예외를 던진다")
    void throwsWhenNoMobilePlan() {
        // given: 회원은 모바일 활성 구독이 없고, 모바일 요금제 후보도 0개인 상황
        Member member = member(2L);
        when(subscriptionRepository.findActiveByMemberIdAndProductType(2L, ProductType.MOBILE_PLAN))
                .thenReturn(Optional.empty());
        when(productRepository.countByProductType(ProductType.MOBILE_PLAN)).thenReturn(0L);

        // when/then: NOT_FOUND(mobilePlan) 예외가 발생해야 한다.
        assertThatThrownBy(() -> useCase.assignForNewMember(member))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException custom = (CustomException) ex;
                    assertThat(custom.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(custom.getField()).isEqualTo("mobilePlan");
                });
    }

    @Test
    @DisplayName("요금제 1개가 있으면 해당 요금제로 활성 구독을 생성한다")
    void assignsRandomMobilePlan() {
        // given: 회원은 모바일 활성 구독이 없고, 모바일 요금제 후보가 1개인 상황
        Member member = member(3L);
        Product plan = mobilePlan(100L);

        when(subscriptionRepository.findActiveByMemberIdAndProductType(3L, ProductType.MOBILE_PLAN))
                .thenReturn(Optional.empty());
        when(productRepository.countByProductType(ProductType.MOBILE_PLAN)).thenReturn(1L);
        when(productRepository.findByProductType(eq(ProductType.MOBILE_PLAN), eq(PageRequest.of(0, 1))))
                .thenReturn(new PageImpl<>(List.of(plan)));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        // when: 초기 요금제 자동 할당 실행
        useCase.assignForNewMember(member);

        // then: 선택된 요금제로 활성 구독이 생성되어 저장된다.
        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        Subscription saved = captor.getValue();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getProduct()).isEqualTo(plan);
        assertThat(saved.getStatus()).isTrue();
        assertThat(saved.getEndDate()).isNull();
        assertThat(saved.getStartDate()).isNotNull();
    }

    private Member member(Long id) {
        // 테스트용 Member 생성 후 리플렉션으로 id 주입
        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Product mobilePlan(Long id) {
        // 테스트용 모바일 요금제(ProductType.MOBILE_PLAN) 생성
        return Product.builder()
                .productId(id)
                .productCode("PLAN_MOB_TEST")
                .name("테스트 요금제")
                .price(10000)
                .salePrice(9000)
                .productType(ProductType.MOBILE_PLAN)
                .discountType("테스트 할인")
                .build();
    }
}
