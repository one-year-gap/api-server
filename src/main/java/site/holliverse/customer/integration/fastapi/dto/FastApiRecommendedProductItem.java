package site.holliverse.customer.integration.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * FastAPI recommended_products[] 항목. 내려주는 그대로 (rank, productId, productName, ...).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FastApiRecommendedProductItem(
        Integer rank,
        @JsonProperty("productId")
        Long productId,
        String productName,
        String productType,
        Integer productPrice,
        Integer salePrice,
        List<String> tags,
        @JsonProperty("reason")
        String reason
) {}
