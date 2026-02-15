package site.holliverse.customer.application.usecase.dto;

public record InternetDetailDto(
        Long productId,
        Integer speedMbps,
        String addonBenefit
) {}
