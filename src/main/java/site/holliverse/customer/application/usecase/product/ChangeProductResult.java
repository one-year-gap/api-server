package site.holliverse.customer.application.usecase.product;

import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.Subscription;

import java.time.LocalDateTime;

public record ChangeProductResult(
        Long subscriptionId,
        Long productId,
        String productName,
        Integer salePrice,
        LocalDateTime startDate
) {
    public static ChangeProductResult from(Subscription subscription, Product product) {
        return new ChangeProductResult(
                subscription.getId(),
                product.getProductId(),
                product.getName(),
                product.getSalePrice(),
                subscription.getStartDate()
        );
    }
}
