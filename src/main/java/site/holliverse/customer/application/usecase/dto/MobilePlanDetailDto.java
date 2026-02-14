package site.holliverse.customer.application.usecase.dto;

public record MobilePlanDetailDto(
        Long productId,
        String dataAmount,
        Integer tetheringSharingData,
        String benefitBrands,
        String benefitVoiceCall,
        String benefitSms,
        String benefitMedia,
        String benefitPremium,
        String benefitSignatureFamilyDiscount
) {}
