package site.holliverse.customer.web.dto.product;

public record AddonContent(
        String addonType,
        String description
) implements ProductContent {
}
