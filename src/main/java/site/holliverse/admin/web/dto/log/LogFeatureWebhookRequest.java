package site.holliverse.admin.web.dto.log;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import site.holliverse.admin.application.usecase.UserActionFeatureEventName;

import java.time.Instant;

public record LogFeatureWebhookRequest(

        @NotNull(message = "eventType은 필수입니다.")
        UserActionFeatureEventName eventType,

        @NotNull(message = "memberId는 필수입니다.")
        @Positive(message = "memberId는 1 이상이어야 합니다.")
        Long memberId,

        @NotNull(message = "timeStamp는 필수입니다.")
        Instant timeStamp
) {
}
