package site.holliverse.customer.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.customer.application.usecase.recommendation.RecommendationResult;
import site.holliverse.customer.application.usecase.recommendation.RecommendationService;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.recommendation.RecommendationResponse;
import site.holliverse.customer.web.mapper.RecommendationResponseMapper;
import site.holliverse.shared.security.CustomUserDetails;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/customer/recommendations")
@Profile("customer")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final RecommendationResponseMapper recommendationResponseMapper;

    /**
     * 캐시 우선: DB에 유효 캐시가 있으면 반환, 없거나 만료 시 FastAPI 호출 후 반환.
     */
    @GetMapping
    public ApiResponse<RecommendationResponse> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        RecommendationResult result = recommendationService.getRecommendations(customUserDetails.getMemberId());
        return new ApiResponse<>("success", recommendationResponseMapper.toResponse(result), LocalDateTime.now());
    }

    /**
     * 강제 갱신: 항상 FastAPI 호출 후 DB 저장·반환.
     */
    @PostMapping("/refresh")
    public ApiResponse<RecommendationResponse> refreshRecommendations(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        RecommendationResult result = recommendationService.refreshRecommendations(customUserDetails.getMemberId());
        return new ApiResponse<>("success", recommendationResponseMapper.toResponse(result), LocalDateTime.now());
    }
}
