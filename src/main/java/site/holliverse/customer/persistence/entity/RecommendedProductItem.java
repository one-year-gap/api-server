package site.holliverse.customer.persistence.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * recommended_products JSONB 항목. 내려주는 그대로 저장 (rank, productId, productName, productType, productPrice, salePrice, tags, reason).
 * 기존 DB 데이터(product_id, llmReason 등) 하위 호환을 위해 @JsonAlias 유지.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RecommendedProductItem(
        Integer rank,
        @JsonProperty("productId") @JsonAlias("product_id")
        Long productId,
        @JsonProperty("productName") @JsonAlias("product_name")
        String productName,
        @JsonProperty("productType") @JsonAlias("product_type")
        String productType,
        @JsonProperty("productPrice") @JsonAlias("product_price")
        Integer productPrice,
        @JsonProperty("salePrice") @JsonAlias("sale_price")
        Integer salePrice,
        List<String> tags,
        @JsonProperty("reason") @JsonAlias("llmReason")
        String reason
) {}
