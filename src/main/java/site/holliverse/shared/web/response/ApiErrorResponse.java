package site.holliverse.shared.web.response;

import java.time.Instant;

public record ApiErrorResponse(
        String status,
        String message,
        ApiErrorDetail errorDetail,
        Instant timestamp
) {
    public static ApiErrorResponse error(String message, ApiErrorDetail detail) {
        return new ApiErrorResponse("error", message, detail, Instant.now());
    }
}
