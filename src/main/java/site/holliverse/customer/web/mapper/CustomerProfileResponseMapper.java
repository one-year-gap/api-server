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
                result.mobilePlan().usageDetails()
        );

        return new CustomerProfileResponse(
                result.name(),
                result.membership(),
                maskPhone(result.phone()),
                subscriptions,
                mobilePlan
        );
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        String digits = phone.replaceAll("\\D", "");
        if (digits.length() < 7) {
            return "****";
        }

        String prefix = digits.substring(0, Math.min(3, digits.length()));
        String suffix = digits.substring(digits.length() - 4);
        return prefix + "-****-" + suffix;
    }
}
