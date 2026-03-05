package site.holliverse.customer.web.dto.member;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    /** 모바일 요금제 당월 사용량 상세 (API JSON: data_gb, sms_cnt, voice_min) */
    public record UsageDetails(
            @JsonProperty("data_gb") Double dataGb,
            @JsonProperty("sms_cnt") Integer smsCnt,
            @JsonProperty("voice_min") Integer voiceMin
    ) {}

    public record MobilePlanDetail(
            String dataAmount,
            boolean isDay,
            String benefitSms,
            String benefitVoiceCall,
            UsageDetails usageDetails
    ) {}
}
