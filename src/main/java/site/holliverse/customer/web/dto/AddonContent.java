package site.holliverse.customer.web.dto;

public record AddonContent(
        String addon_type,
        String description
) implements ProductContent {
}
