package site.holliverse.customer.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * recommended_products JSONB 항목. 내려주는 그대로 저장 (rank, productId, productName, productType, productPrice, salePrice, tags, reason).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RecommendedProductItem(
        Integer rank,
        Long productId,
        String productName,
        String productType,
        Integer productPrice,
        Integer salePrice,
        List<String> tags,
        String reason
) {}
