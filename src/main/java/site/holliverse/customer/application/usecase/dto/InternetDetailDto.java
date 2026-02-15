package site.holliverse.customer.application.usecase.dto;

public record InternetDetailDto(
        Long productId,
        String planTitle,
        String speed,
        String addonBenefit,
        String benefits
) {}
