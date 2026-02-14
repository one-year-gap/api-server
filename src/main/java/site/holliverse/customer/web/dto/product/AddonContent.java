package site.holliverse.customer.web.dto.product;

public record AddonContent(
        String addon_type,
        String description
) implements ProductContent {
}
