package site.holliverse.customer.web.dto.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UserLogRequest(
        @NotNull
        @JsonProperty("event_id")
        String tsid,

        @NotBlank
        @Size(max = 64)
        String timestamp,

        @NotBlank
        @Size(max = 64)
        String event,

        @NotBlank
        @JsonProperty("event_name")
        @Size(max = 128)
        String eventName,

        @NotNull
        @JsonProperty("event_properties")
        Map<String, Object> eventProperties
) {
}

