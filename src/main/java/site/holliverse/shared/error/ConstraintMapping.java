package site.holliverse.shared.error;

public record ConstraintMapping(
        ErrorCode errorCode,
        String field
) {
}
