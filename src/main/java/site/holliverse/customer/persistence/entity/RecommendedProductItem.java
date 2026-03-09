package site.holliverse.customer.persistence.entity;

/**
 * recommended_products JSONB 항목 (productId, 추천 이유 문구).
 */
public record RecommendedProductItem(Long productId, String reason) {}
