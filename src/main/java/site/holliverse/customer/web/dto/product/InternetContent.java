package site.holliverse.customer.web.dto.product;

public record InternetContent(
        Integer speed_mbps,
        String addon_benefit
) implements ProductContent {
}
