package site.holliverse.customer.web.mapper;

import site.holliverse.customer.application.usecase.member.CustomerProfileResult;
import site.holliverse.customer.web.dto.member.CustomerProfileResponse;

import java.util.List;

public class CustomerProfileResponseMapper {

    public CustomerProfileResponse toResponse(CustomerProfileResult result) {
        List<CustomerProfileResponse.SubscriptionItem> subscriptions = result.subscriptions().stream()
                .map(item -> new CustomerProfileResponse.SubscriptionItem(
                        item.subscriptionId(),
                        item.productName(),
                        item.productType()
                ))
                .toList();

        CustomerProfileResponse.MobilePlanDetail mobilePlan = result.mobilePlan() == null
                ? null
                : new CustomerProfileResponse.MobilePlanDetail(
                result.mobilePlan().dataAmount(),
                result.mobilePlan().isDay(),
                result.mobilePlan().benefitSms(),
                result.mobilePlan().benefitVoiceCall(),
                toUsageDetails(result.mobilePlan().usageDetails())
        );

        return new CustomerProfileResponse(
                result.name(),
                result.membership(),
                result.phone(),
                subscriptions,
                mobilePlan
        );
    }

    private CustomerProfileResponse.UsageDetails toUsageDetails(CustomerProfileResult.UsageDetails details) {
        if (details == null) {
            return new CustomerProfileResponse.UsageDetails(null, null, null);
        }
        return new CustomerProfileResponse.UsageDetails(
                details.dataGb(),
                details.smsCnt(),
                details.voiceMin()
        );
    }
}
