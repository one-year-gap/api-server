package site.holliverse.customer.web.dto;

public record IptvContent(
        Integer channel_count,
        String addon_benefit
) implements ProductContent {
}
