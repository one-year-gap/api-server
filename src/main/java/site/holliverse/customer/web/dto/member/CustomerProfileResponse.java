package site.holliverse.customer.web.dto.member;

import site.holliverse.shared.domain.model.MemberMembership;
import site.holliverse.shared.domain.model.ProductType;

import java.util.List;
import java.util.Map;

public record CustomerProfileResponse(
        String name,
        MemberMembership membership,
        String phone,
        List<SubscriptionItem> subscriptions,
        MobilePlanDetail mobilePlan
) {

    public record SubscriptionItem(
            Long subscriptionId,
            String productName,
            ProductType productType
    ) {}

    public record MobilePlanDetail(
            String dataAmount,
            boolean isDay,
            String benefitSms,
            String benefitVoiceCall,
            Map<String, Object> usageDetails
    ) {}
}
