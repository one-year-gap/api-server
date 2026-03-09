package site.holliverse.customer.web.dto.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class UserLogRequest {

    @NotNull
    @JsonProperty("event_id")
    private Long eventId;

    @NotBlank
    private String timestamp;

    @NotBlank
    private String event;

    @NotBlank
    @JsonProperty("event_name")
    private String eventName;

    @NotNull
    @JsonProperty("event_properties")
    private Map<String, Object> eventProperties;
}

