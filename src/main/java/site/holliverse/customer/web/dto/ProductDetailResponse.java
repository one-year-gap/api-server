package site.holliverse.customer.web.dto;

public record ProductDetailResponse(
        Long product_id,
        String name,
        String product_type,
        Integer price,
        Integer sale_price,
        String discount_type,
        String product_code,
        ProductContent content
) {}
