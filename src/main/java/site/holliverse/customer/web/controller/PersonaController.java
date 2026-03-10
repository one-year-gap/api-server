package site.holliverse.customer.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.customer.application.usecase.persona.GetMyPersonaUseCase;
import site.holliverse.customer.application.usecase.persona.PersonaDetailResult;
import site.holliverse.customer.web.dto.ApiResponse;
import site.holliverse.customer.web.dto.persona.PersonaDetailResponse;
import site.holliverse.shared.security.CustomUserDetails;

import java.time.LocalDateTime;

/**
 * 로그인한 고객의 "내 페르소나 상세"를 내려주는 컨트롤러.
 *
 * 핵심 역할:
 * 1) SecurityContext 에서 인증 사용자(memberId) 추출
 * 2) UseCase 실행
 * 3) UseCase 결과를 API 응답 DTO 형태로 변환
 */
@RestController
@RequestMapping("/api/v1/customer/persona-types")
@Profile("customer")
@RequiredArgsConstructor
public class PersonaController {

    private final GetMyPersonaUseCase getMyPersonaUseCase;

    @GetMapping("/me")
    public ApiResponse<PersonaDetailResponse> getPersonaDetail(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        // 인증 컨텍스트가 비어 있으면 명시적으로 401을 반환한다.
        // (과거 user.getMemberId()에서 NPE가 발생했던 케이스 방지)
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        // 토큰에서 추출된 memberId 기준으로 유스케이스 실행
        PersonaDetailResult result = getMyPersonaUseCase.execute(user.getMemberId());

        // 내부 Result 모델을 외부 응답 DTO로 매핑
        PersonaDetailResponse response = new PersonaDetailResponse(
                result.personaTypeId(),
                result.characterName(),
                result.shortDesc(),
                result.characterDescription(),
                result.version(),
                result.isActive(),
                result.tags(),
                new PersonaDetailResponse.TscoreIndex(
                        result.tscoreIndex().snapshotDate(),
                        result.tscoreIndex().exploreTscore(),
                        result.tscoreIndex().benefitTrendTscore(),
                        result.tscoreIndex().multiDeviceTscore(),
                        result.tscoreIndex().familyHomeTscore(),
                        result.tscoreIndex().internetSecurityTscore(),
                        result.tscoreIndex().stabilityTscore()
                )
        );

        // 프로젝트 공통 ApiResponse 래퍼로 반환
        return new ApiResponse<>("success", response, LocalDateTime.now());
    }
}
