package site.holliverse.customer.application.usecase.dto;

import site.holliverse.shared.domain.model.ProductType;

public record ProductSummaryDto(
        Long productId,
        String name,
        Integer price,
        Integer salePrice,
        ProductType productType,
        String productCode,
        String discountType
) {}
