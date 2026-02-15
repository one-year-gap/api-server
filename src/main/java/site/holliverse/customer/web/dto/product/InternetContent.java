package site.holliverse.customer.web.dto.product;

public record InternetContent(
        String planTitle,
        String speed,
        String benefits
) implements ProductContent {
}
