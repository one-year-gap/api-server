package site.holliverse.customer.application.usecase.member;

import site.holliverse.shared.domain.model.MemberMembership;
import site.holliverse.shared.domain.model.ProductType;

import java.util.List;
import java.util.Map;

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

    public record MobilePlanInfo(
            String dataAmount,
            String benefitSms,
            String benefitVoiceCall,
            Map<String, Object> usageDetails // 모바일 요금제 사용량 상세정보
    ) {}
}
