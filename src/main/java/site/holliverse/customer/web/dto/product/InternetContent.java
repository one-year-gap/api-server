package site.holliverse.customer.web.dto.product;

public record InternetContent(
        Integer speedMbps,
        String addonBenefit
) implements ProductContent {
}
