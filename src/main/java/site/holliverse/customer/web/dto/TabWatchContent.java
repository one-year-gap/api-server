package site.holliverse.customer.web.dto;

public record TabWatchContent(
        String data_amount,
        String benefit_voice_call,
        String benefit_sms
) implements ProductContent {
}
