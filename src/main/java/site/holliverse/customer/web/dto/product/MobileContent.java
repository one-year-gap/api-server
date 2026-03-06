package site.holliverse.customer.web.dto.product;

public record MobileContent(
        String dataAmount,
        Integer tetheringSharingData,
        String benefitBrands,
        String benefitVoiceCall,
        String benefitSms,
        String benefitMedia,
        String benefitPremium,
        String benefitSignatureFamilyDiscount
) implements ProductContent {
}
