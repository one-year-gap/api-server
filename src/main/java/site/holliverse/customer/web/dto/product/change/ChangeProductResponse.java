package site.holliverse.customer.web.dto.product.change;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * 요금제 변경/신규 가입 API 응답 body. snake_case 적용.
 */
public record ChangeProductResponse(
        @JsonProperty("subscription_id") Long subscriptionId,
        @JsonProperty("product_id") Long productId,
        @JsonProperty("product_name") String productName,
        @JsonProperty("sale_price") Integer salePrice,
        @JsonProperty("start_date") LocalDateTime startDate
) {}
