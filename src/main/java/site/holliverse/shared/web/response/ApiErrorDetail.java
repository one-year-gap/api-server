package site.holliverse.shared.web.response;

public record ApiErrorDetail(
        String code,
        String field,
        String reason
) {
}
