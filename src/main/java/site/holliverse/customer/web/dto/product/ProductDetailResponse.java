package site.holliverse.customer.web.dto.product;

public record ProductDetailResponse(
        Long productId,
        String name,
        String productType,
        Integer price,
        Integer salePrice,
        String discountType,
        String productCode,
        ProductContent content,
        boolean isBest
) {}
