package site.holliverse.customer.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.customer.application.usecase.counsel.CreateCounselUseCase;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.counsel.CreateCounselDto;
import site.holliverse.shared.security.CustomUserDetails;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/customer")
@Profile("customer")
@RequiredArgsConstructor
@Validated
public class CounselController {
    private final CreateCounselUseCase useCase;

    @PostMapping("/counsel")
    public ApiResponse<Long> createCounsel(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody CreateCounselDto request
            ){

        Long counselId = useCase.execute(customUserDetails.getMemberId(),request.title(),request.content());
        return new ApiResponse<>("created",counselId, LocalDateTime.now());
    }
}
