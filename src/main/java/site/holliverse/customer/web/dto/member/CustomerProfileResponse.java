package site.holliverse.customer.web.dto.member;

import site.holliverse.shared.domain.model.MemberMembership;
import site.holliverse.shared.domain.model.ProductType;

import java.util.List;

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

    /** 모바일 요금제 당월 사용량 상세 (camelCase: dataGb, smsCnt, voiceMin) */
    public record UsageDetails(
            Double dataGb,
            Integer smsCnt,
            Integer voiceMin
    ) {}

    public record MobilePlanDetail(
            String dataAmount,
            boolean isDay,
            String benefitSms,
            String benefitVoiceCall,
            UsageDetails usageDetails
    ) {}
}
