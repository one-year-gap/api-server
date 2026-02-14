package site.holliverse.customer.web.dto;

import java.util.List;

public record ProductListResponse(
        PageMeta page,
        List<ProductDetailResponse> content
) {}
