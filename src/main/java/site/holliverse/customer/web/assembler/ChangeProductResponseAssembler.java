package site.holliverse.customer.web.assembler;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import site.holliverse.customer.application.usecase.product.ChangeProductResult;
import site.holliverse.customer.web.dto.product.change.ChangeProductResponse;

/**
 * ChangeProductResult(Application) → ChangeProductResponse(Web) 조립.
 */
@Component
@Profile("customer")
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
