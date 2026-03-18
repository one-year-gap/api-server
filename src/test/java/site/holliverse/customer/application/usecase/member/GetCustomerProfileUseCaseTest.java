package site.holliverse.customer.application.usecase.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.persistence.entity.MobilePlan;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.customer.persistence.entity.UsageMonthly;
import site.holliverse.customer.persistence.repository.MobilePlanRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.customer.persistence.repository.UsageMonthlyRepository;
import site.holliverse.shared.domain.model.MemberMembership;
import site.holliverse.shared.domain.model.ProductType;
import site.holliverse.shared.persistence.entity.Address;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.MemberRepository;
import site.holliverse.shared.util.DecryptionTool;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCustomerProfileUseCase unit test")
class GetCustomerProfileUseCaseTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private MobilePlanRepository mobilePlanRepository;

    @Mock
    private UsageMonthlyRepository usageMonthlyRepository;

    @Mock
    private DecryptionTool decryptionTool;

    @InjectMocks
    private GetCustomerProfileUseCase useCase;

    @Test
    @DisplayName("execute returns email, address, birthDate, and contract for active mobile subscription")
    void execute_returnsProfileAndContract() {
        Long memberId = 1L;
        Member member = createMember(memberId);
        Subscription subscription = createMobileSubscription(member);
        MobilePlan mobilePlan = createMobilePlan(subscription.getProduct());
        UsageMonthly usageMonthly = createUsageMonthly(subscription);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(subscriptionRepository.findAllActiveByMemberId(memberId)).willReturn(List.of(subscription));
        given(mobilePlanRepository.findById(subscription.getProduct().getProductId())).willReturn(Optional.of(mobilePlan));
        given(usageMonthlyRepository.findFirstBySubscription_IdOrderByYyyymmDesc(subscription.getId()))
                .willReturn(Optional.of(usageMonthly));
        given(decryptionTool.decrypt("enc-name")).willReturn("Kim");
        given(decryptionTool.decrypt("enc-phone")).willReturn("01012345678");

        CustomerProfileResult result = useCase.execute(memberId);

        assertThat(result.name()).isEqualTo("Kim");
        assertThat(result.email()).isEqualTo("kim@example.com");
        assertThat(result.phone()).isEqualTo("01012345678");
        assertThat(result.address()).isEqualTo("Seoul Gangnam Teheran-ro 123");
        assertThat(result.birthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(result.contract()).isNotNull();
        assertThat(result.contract().contractStartDate()).isEqualTo(LocalDate.of(2026, 3, 18));
        assertThat(result.contract().contractEndDate()).isEqualTo(LocalDate.of(2028, 3, 18));
        assertThat(result.contract().contractMonths()).isEqualTo(24);
        assertThat(result.mobilePlan()).isNotNull();
        assertThat(result.mobilePlan().dataAmount()).isEqualTo("100GB");
        assertThat(result.mobilePlan().usageDetails().dataGb()).isEqualTo(2.8);
    }

    @Test
    @DisplayName("execute returns null contract when there is no active mobile subscription")
    void execute_returnsNullContractWithoutMobileSubscription() {
        Long memberId = 2L;
        Member member = createMember(memberId);
        Product addonProduct = Product.builder()
                .productId(10L)
                .productCode("ADDON-1")
                .name("Addon")
                .price(1000)
                .salePrice(1000)
                .productType(ProductType.ADDON)
                .discountType("none")
                .build();
        Subscription addonSubscription = Subscription.builder()
                .id(99L)
                .member(member)
                .product(addonProduct)
                .startDate(LocalDateTime.of(2026, 3, 18, 0, 0))
                .status(true)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(subscriptionRepository.findAllActiveByMemberId(memberId)).willReturn(List.of(addonSubscription));
        given(decryptionTool.decrypt("enc-name")).willReturn("Kim");
        given(decryptionTool.decrypt("enc-phone")).willReturn("01012345678");

        CustomerProfileResult result = useCase.execute(memberId);

        assertThat(result.contract()).isNull();
        assertThat(result.mobilePlan()).isNull();
        assertThat(result.subscriptions()).hasSize(1);
        assertThat(result.subscriptions().get(0).productType()).isEqualTo(ProductType.ADDON);
    }

    private Member createMember(Long memberId) {
        Address address = Address.builder()
                .id(1L)
                .province("Seoul")
                .city("Gangnam")
                .streetAddress("Teheran-ro 123")
                .postalCode("12345")
                .build();

        return Member.builder()
                .id(memberId)
                .address(address)
                .email("kim@example.com")
                .name("enc-name")
                .phone("enc-phone")
                .birthDate(LocalDate.of(2000, 1, 1))
                .membership(MemberMembership.GOLD)
                .joinDate(LocalDate.of(2026, 3, 1))
                .build();
    }

    private Subscription createMobileSubscription(Member member) {
        Product product = Product.builder()
                .productId(5L)
                .productCode("PLAN-5")
                .name("5G Plan")
                .price(50000)
                .salePrice(45000)
                .productType(ProductType.MOBILE_PLAN)
                .discountType("none")
                .build();

        return Subscription.builder()
                .id(50L)
                .member(member)
                .product(product)
                .startDate(LocalDateTime.of(2026, 3, 18, 0, 0))
                .contractMonths(24)
                .contractEndDate(LocalDateTime.of(2028, 3, 18, 0, 0))
                .status(true)
                .build();
    }

    private MobilePlan createMobilePlan(Product product) {
        return MobilePlan.builder()
                .productId(product.getProductId())
                .product(product)
                .dataAmount("100GB")
                .benefitVoiceCall("Unlimited")
                .benefitSms("Basic")
                .build();
    }

    private UsageMonthly createUsageMonthly(Subscription subscription) {
        return UsageMonthly.builder()
                .id(1L)
                .subscription(subscription)
                .yyyymm(YearMonth.of(2026, 3))
                .usageDetails(Map.of(
                        "data_gb", 2.8,
                        "sms_cnt", 54,
                        "voice_min", 145
                ))
                .build();
    }
}
