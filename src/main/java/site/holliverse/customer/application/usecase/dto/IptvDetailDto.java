package site.holliverse.customer.application.usecase.dto;

public record IptvDetailDto(
        Long productId,
        String planTitle,
        Integer channelCount,
        String benefits
) {}
