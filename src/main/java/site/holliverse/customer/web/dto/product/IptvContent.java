package site.holliverse.customer.web.dto.product;

public record IptvContent(
        Integer channelCount,
        String addonBenefit
) implements ProductContent {
}
