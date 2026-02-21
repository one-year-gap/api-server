package site.holliverse.customer.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.shared.security.CustomUserDetails;
import site.holliverse.customer.application.usecase.product.ChangeProductResult;
import site.holliverse.customer.application.usecase.product.ChangeProductUseCase;
import site.holliverse.customer.web.assembler.ChangeProductResponseAssembler;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.product.change.ChangeProductRequest;
import site.holliverse.customer.web.dto.product.change.ChangeProductResponse;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/customer/plans")
@Profile("customer")
@RequiredArgsConstructor
public class SubscriptionController {

    private final ChangeProductUseCase changeProductUseCase;
    private final ChangeProductResponseAssembler changeProductResponseAssembler;

    @PostMapping("/change")
    public ApiResponse<ChangeProductResponse> changePlan(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody ChangeProductRequest request) {
        if (customUserDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        Long memberId = customUserDetails.getMemberId(); // memberId 추출 
        ChangeProductResult result = changeProductUseCase.execute(memberId, request.targetProductId()); // UseCase 호출
        ChangeProductResponse response = changeProductResponseAssembler.assemble(result);
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }
}
