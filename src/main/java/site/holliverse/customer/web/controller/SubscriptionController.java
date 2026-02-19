package site.holliverse.customer.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.customer.application.usecase.product.ChangeProductResult;
import site.holliverse.customer.application.usecase.product.ChangeProductUseCase;
import site.holliverse.customer.web.assembler.ChangeProductResponseAssembler;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.product.change.ChangeProductRequest;
import site.holliverse.customer.web.dto.product.change.ChangeProductResponse;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/plans")
@Profile("customer")
@RequiredArgsConstructor
public class SubscriptionController {

    private final ChangeProductUseCase changeProductUseCase;
    private final ChangeProductResponseAssembler changeProductResponseAssembler;

    @PostMapping("/change")
    public ApiResponse<ChangeProductResponse> changePlan(@RequestBody ChangeProductRequest request) {
        ChangeProductResult result = changeProductUseCase.execute(request.memberId(), request.targetProductId());
        ChangeProductResponse response = changeProductResponseAssembler.assemble(result);
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }
}
