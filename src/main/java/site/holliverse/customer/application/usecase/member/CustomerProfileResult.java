package site.holliverse.customer.application.usecase.member;

import site.holliverse.shared.domain.model.MemberMembership;
import site.holliverse.shared.domain.model.ProductType;

import java.util.List;

public record CustomerProfileResult(
        String name,
        MemberMembership membership,
        String phone,
        List<SubscriptionSummaryItem> subscriptions, //구독하고 있는 상품
        MobilePlanInfo mobilePlan // 모바일 요금제 상세정보
) {

    public record SubscriptionSummaryItem(
            Long subscriptionId,
            String productName,
            ProductType productType
    ) {}

    /** 모바일 요금제 당월 사용량 상세 (data_gb, sms_cnt, voice_min) */
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
}
