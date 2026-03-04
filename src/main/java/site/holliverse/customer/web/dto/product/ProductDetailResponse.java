package site.holliverse.customer.web.dto.product;

import java.util.List;

public record ProductDetailResponse(
        Long productId,
        String name,
        String productType,
        Integer price,
        Integer salePrice,
        String discountType,
        String productCode,
        List<String> tags,
        ProductContent content,
        boolean isBest
) {}
