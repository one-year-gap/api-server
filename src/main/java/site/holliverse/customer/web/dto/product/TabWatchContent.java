package site.holliverse.customer.web.dto.product;

public record TabWatchContent(
        String dataAmount,
        String benefitVoiceCall,
        String benefitSms
) implements ProductContent {
}
