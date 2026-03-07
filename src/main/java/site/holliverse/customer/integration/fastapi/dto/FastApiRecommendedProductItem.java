package site.holliverse.customer.integration.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * FastAPI recommended_products[] 항목 (product_id, reason).
 */
public record FastApiRecommendedProductItem(
        @JsonProperty("product_id")
        Long productId,
        String reason
) {}
