package site.holliverse.customer.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.customer.application.usecase.log.UserLogService;
import site.holliverse.customer.web.dto.log.UserLogRequest;
import site.holliverse.shared.security.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/user-logs")
@Profile("customer")
@RequiredArgsConstructor
public class UserLogController {

    private final UserLogService userLogService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void log(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid List<UserLogRequest> requests
    ) {
        Long memberId = customUserDetails.getMemberId();
        userLogService.publishBatch(memberId, requests);
    }
}

