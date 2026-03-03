package site.holliverse.shared.alert.web.dto;

public record AlertAckRequest(
        String actor,
        String comment,
        String runbookUrl
) {
}
