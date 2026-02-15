package site.holliverse.customer.web.dto;

public record PageMeta(
        long totalElements,
        int totalPages,
        int number,
        int size
) {}
