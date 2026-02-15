package site.holliverse.customer.web.dto.product;

public record IptvContent(
        String planTitle,
        Integer channelCount,
        String benefits
) implements ProductContent {
}
