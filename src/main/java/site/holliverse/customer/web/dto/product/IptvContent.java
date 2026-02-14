package site.holliverse.customer.web.dto.product;

public record IptvContent(
        Integer channel_count,
        String addon_benefit
) implements ProductContent {
}
