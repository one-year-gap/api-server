package site.holliverse.customer.web.dto.product;

public record MobileContent(
        String data_amount,
        Integer tethering_sharing_data,
        String benefit_brands,
        String benefit_voice_call,
        String benefit_sms,
        String benefit_media,
        String benefit_premium,
        String benefit_signature_family_discount
) implements ProductContent {
}
