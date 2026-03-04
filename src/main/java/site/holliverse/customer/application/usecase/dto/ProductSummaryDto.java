package site.holliverse.customer.application.usecase.dto;

import site.holliverse.shared.domain.model.ProductType;
import java.util.List;

public record ProductSummaryDto(
        Long productId,
        String name,
        Integer price,
        Integer salePrice,
        ProductType productType,
        String productCode,
        String discountType,
        List<String> tags
) {}
