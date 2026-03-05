package site.holliverse.customer.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProductViewHistoryId implements Serializable {

    private Long memberId;
    private Long productId;

    public ProductViewHistoryId() {}

    public ProductViewHistoryId(Long memberId, Long productId) {
        this.memberId = memberId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductViewHistoryId that)) return false;
        return Objects.equals(memberId, that.memberId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, productId);
    }
}
