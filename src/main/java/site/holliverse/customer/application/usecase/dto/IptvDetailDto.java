package site.holliverse.customer.application.usecase.dto;

public record IptvDetailDto(
        Long productId,
        Integer channelCount,
        String addonBenefit
) {}
