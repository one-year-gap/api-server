package site.holliverse.customer.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.customer.application.usecase.member.CustomerProfileResult;
import site.holliverse.customer.application.usecase.member.GetCustomerProfileUseCase;
import site.holliverse.customer.application.usecase.member.GetRecentActivitiesUseCase;
import site.holliverse.customer.application.usecase.member.RecentActivityResult;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.member.CustomerProfileResponse;
import site.holliverse.customer.web.dto.member.RecentActivityResponse;
import site.holliverse.customer.web.mapper.CustomerProfileResponseMapper;
import site.holliverse.customer.web.mapper.RecentActivityResponseMapper;
import site.holliverse.shared.security.CustomUserDetails;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/customer")
@Profile("customer")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final GetCustomerProfileUseCase getCustomerProfileUseCase;
    private final CustomerProfileResponseMapper customerProfileResponseMapper;
    private final GetRecentActivitiesUseCase getRecentActivitiesUseCase;
    private final RecentActivityResponseMapper recentActivityResponseMapper;

    @GetMapping("/me")
    public ApiResponse<CustomerProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        CustomerProfileResult result = getCustomerProfileUseCase.execute(customUserDetails.getMemberId());
        CustomerProfileResponse response = customerProfileResponseMapper.toResponse(result);
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }

    @GetMapping("/recent-activities")
    public ApiResponse<RecentActivityResponse> getRecentActivities(
            @RequestParam(value = "limit", defaultValue = "3") int limit,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        if (limit < 1 || limit > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "제한 범위를 초과했습니다. (1-50)");
        }

        RecentActivityResult result = getRecentActivitiesUseCase.execute(customUserDetails.getMemberId(), limit);
        RecentActivityResponse response = recentActivityResponseMapper.toResponse(result);

        return new ApiResponse<>("success", response, LocalDateTime.now());
    }
}
