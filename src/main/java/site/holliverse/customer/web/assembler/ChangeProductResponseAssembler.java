package site.holliverse.customer.web.assembler;

import site.holliverse.customer.application.usecase.product.ChangeProductResult;
import site.holliverse.customer.web.dto.product.change.ChangeProductResponse;

/**
 * ChangeProductResult(Application) → ChangeProductResponse(Web) 조립.
 */
public class ChangeProductResponseAssembler {

    public ChangeProductResponse assemble(ChangeProductResult result) {
        return new ChangeProductResponse(
                result.subscriptionId(),
                result.productId(),
                result.productName(),
                result.salePrice(),
                result.startDate()
        );
    }
}
