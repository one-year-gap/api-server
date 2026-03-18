package site.holliverse.customer.application.usecase.member;

import site.holliverse.shared.domain.model.MemberMembership;
import site.holliverse.shared.domain.model.ProductType;

import java.time.LocalDate;
import java.util.List;

public record CustomerProfileResult(
        String name,
        MemberMembership membership,
        String email,
        String phone,
        String address,
        LocalDate birthDate,
        List<SubscriptionSummaryItem> subscriptions,
        MobilePlanInfo mobilePlan,
        ContractInfo contract
) {

    public record SubscriptionSummaryItem(
            Long subscriptionId,
            String productName,
            ProductType productType
    ) {}

    public record UsageDetails(
            Double dataGb,
            Integer smsCnt,
            Integer voiceMin
    ) {}

    public record MobilePlanInfo(
            String dataAmount,
            boolean isDay,
            String benefitSms,
            String benefitVoiceCall,
            UsageDetails usageDetails
    ) {}

    public record ContractInfo(
            LocalDate contractStartDate,
            LocalDate contractEndDate,
            Integer contractMonths
    ) {}
}
