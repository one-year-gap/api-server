package site.holliverse.customer.web.dto.product;

public record TabWatchContent(
        String data_amount,
        String benefit_voice_call,
        String benefit_sms
) implements ProductContent {
}
