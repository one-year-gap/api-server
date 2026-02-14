package site.holliverse.customer.web.dto.product;

import site.holliverse.customer.web.dto.PageMeta;

import java.util.List;

public record ProductListResponse(
        PageMeta page,
        List<ProductDetailResponse> content
) {}
