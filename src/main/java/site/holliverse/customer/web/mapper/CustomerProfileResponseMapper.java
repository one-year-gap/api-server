package site.holliverse.customer.web.mapper;

import site.holliverse.customer.application.usecase.member.CustomerProfileResult;
import site.holliverse.customer.web.dto.member.CustomerProfileResponse;
import site.holliverse.customer.web.util.BenefitDisplayUtil;

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
                BenefitDisplayUtil.normalizeBenefitForDisplay(result.mobilePlan().benefitSms()),
                BenefitDisplayUtil.normalizeBenefitForDisplay(result.mobilePlan().benefitVoiceCall()),
                toUsageDetails(result.mobilePlan().usageDetails())
        );

        CustomerProfileResponse.ContractDetail contract = result.contract() == null
                ? null
                : new CustomerProfileResponse.ContractDetail(
                result.contract().contractStartDate(),
                result.contract().contractEndDate(),
                result.contract().contractMonths()
        );

        return new CustomerProfileResponse(
                result.name(),
                result.membership(),
                result.phone(),
                result.email(),
                result.address(),
                result.birthDate(),
                contract,
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
