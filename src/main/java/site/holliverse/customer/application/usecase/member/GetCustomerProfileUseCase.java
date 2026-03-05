package site.holliverse.customer.application.usecase.member;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.persistence.entity.MobilePlan;
import site.holliverse.customer.persistence.entity.Subscription;
import site.holliverse.customer.persistence.repository.MobilePlanRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.customer.persistence.repository.UsageMonthlyRepository;
import site.holliverse.shared.alert.AlertOwner;
import site.holliverse.shared.domain.model.ProductType;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.logging.SystemLogEvent;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.MemberRepository;
import site.holliverse.shared.util.DecryptionTool;

import java.util.List;
import java.util.Map;

@Service
@Profile("customer")
public class GetCustomerProfileUseCase {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MobilePlanRepository mobilePlanRepository;
    private final UsageMonthlyRepository usageMonthlyRepository;
    private final DecryptionTool decryptionTool;

    public GetCustomerProfileUseCase(MemberRepository memberRepository,
                                     SubscriptionRepository subscriptionRepository,
                                     MobilePlanRepository mobilePlanRepository,
                                     UsageMonthlyRepository usageMonthlyRepository,
                                     DecryptionTool decryptionTool) {
        this.memberRepository = memberRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.mobilePlanRepository = mobilePlanRepository;
        this.usageMonthlyRepository = usageMonthlyRepository;
        this.decryptionTool = decryptionTool;
    }

    @Transactional(readOnly = true)
    @SystemLogEvent("customer.member.profile")
    @AlertOwner("hy")
    public CustomerProfileResult execute(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "memberId", "회원을 찾을 수 없습니다: " + memberId));

        List<Subscription> subscriptions = subscriptionRepository.findAllActiveByMemberId(memberId);

        List<CustomerProfileResult.SubscriptionSummaryItem> subscriptionItems = subscriptions.stream()
                .map(subscription -> new CustomerProfileResult.SubscriptionSummaryItem(
                        subscription.getId(),
                        subscription.getProduct().getName(),
                        subscription.getProduct().getProductType()
                ))
                .toList();

        CustomerProfileResult.MobilePlanInfo mobilePlanInfo = buildMobilePlanInfo(subscriptions);

        String decryptedName = safeDecrypt("name", member.getName());
        String decryptedPhone = safeDecrypt("phone", member.getPhone());

        return new CustomerProfileResult(
                decryptedName,
                member.getMembership(),
                decryptedPhone,
                subscriptionItems,
                mobilePlanInfo
        );
    }

    private CustomerProfileResult.MobilePlanInfo buildMobilePlanInfo(List<Subscription> subscriptions) {
        Subscription mobileSubscription = subscriptions.stream()
                .filter(subscription -> subscription.getProduct().getProductType() == ProductType.MOBILE_PLAN)
                .findFirst()
                .orElse(null);

        if (mobileSubscription == null) {
            return null;
        }

        Long productId = mobileSubscription.getProduct().getProductId();
        MobilePlan mobilePlan = mobilePlanRepository.findById(productId).orElse(null);

        if (mobilePlan == null) {
            return null;
        }

        Map<String, Object> usageDetails = usageMonthlyRepository
                .findFirstBySubscription_IdOrderByYyyymmDesc(mobileSubscription.getId())
                .map(usageMonthly -> usageMonthly.getUsageDetails() == null ? Map.<String, Object>of() : usageMonthly.getUsageDetails())
                .orElse(Map.of());

        DataAmountNormalization normalized = normalizeDataAmount(mobilePlan.getDataAmount());

        return new CustomerProfileResult.MobilePlanInfo(
                normalized.dataAmount(),
                normalized.isDay(),
                mobilePlan.getBenefitSms(),
                mobilePlan.getBenefitVoiceCall(),
                usageDetails
        );
    }

    private DataAmountNormalization normalizeDataAmount(String rawDataAmount) {
        if (rawDataAmount == null || rawDataAmount.isBlank()) {
            return new DataAmountNormalization(rawDataAmount, false);
        }

        boolean isDay = rawDataAmount.contains("매일");
        if (!isDay) {
            return new DataAmountNormalization(rawDataAmount, false);
        }

        String normalized = rawDataAmount.replace("매일", "").replaceAll("\\s+", " ").trim();
        return new DataAmountNormalization(normalized, true);
    }

    private String safeDecrypt(String field, String cipherText) {
        if (cipherText == null || cipherText.isBlank()) {
            return null;
        }

        try {
            return decryptionTool.decrypt(cipherText);
        } catch (RuntimeException ex) {
            throw new CustomException(
                    ErrorCode.DECRYPTION_FAILED,
                    field,
                    "복호화에 실패했습니다. 암호화 키/데이터 형식을 확인하세요."
            );
        }
    }

    private record DataAmountNormalization(String dataAmount, boolean isDay) {}

}
