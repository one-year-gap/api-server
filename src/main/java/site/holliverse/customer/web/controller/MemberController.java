package site.holliverse.customer.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.customer.application.usecase.member.CustomerProfileResult;
import site.holliverse.customer.application.usecase.member.GetCustomerProfileUseCase;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.member.CustomerProfileResponse;
import site.holliverse.customer.web.mapper.CustomerProfileResponseMapper;
import site.holliverse.shared.security.CustomUserDetails;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/customer")
@Profile("customer")
@RequiredArgsConstructor
public class MemberController {

    private final GetCustomerProfileUseCase getCustomerProfileUseCase;
    private final CustomerProfileResponseMapper customerProfileResponseMapper;

    @GetMapping("/me")
    public ApiResponse<CustomerProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        CustomerProfileResult result = getCustomerProfileUseCase.execute(customUserDetails.getMemberId());
        CustomerProfileResponse response = customerProfileResponseMapper.toResponse(result);
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }
}
