package site.holliverse.customer.web.dto;

public record InternetContent(
        Integer speed_mbps,
        String addon_benefit
) implements ProductContent {
}
