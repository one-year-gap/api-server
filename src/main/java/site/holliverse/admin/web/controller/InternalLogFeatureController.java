package site.holliverse.admin.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.holliverse.admin.application.usecase.HandleLogFeatureUseCase;
import site.holliverse.admin.application.usecase.LogFeaturesUseCase;
import site.holliverse.admin.web.dto.log.LogFeatureWebhookRequest;
import site.holliverse.admin.web.dto.log.LogFeaturesRequestDto;

/**
 * 실시간 로그 기반 feature customer -> admin 전송 로직
 */
@Profile("admin")
@RestController
@RequestMapping("/internal/v1/log-features")
@RequiredArgsConstructor
public class InternalLogFeatureController {
    private final HandleLogFeatureUseCase useCase;
    private final LogFeaturesUseCase batchUseCase;


    @PostMapping
    public ResponseEntity<Void> receive (@RequestBody @Valid LogFeatureWebhookRequest request){
        useCase.execute(request);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> receiveBatch(@RequestBody @Valid LogFeaturesRequestDto request) {
        batchUseCase.execute(request);

        return ResponseEntity.accepted().build();
    }

}
