package site.holliverse.customer.persistence.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * recommended_products JSONB 항목 (productId, 추천 이유 문구).
 * DB/FastAPI는 product_id, llmReason 등으로 적힌 경우에도 역직렬화되도록 별칭 지원.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RecommendedProductItem(
        @JsonProperty("productId")
        @JsonAlias("product_id")
        Long productId,
        @JsonProperty("reason")
        @JsonAlias("llmReason")
        String reason
) {}
