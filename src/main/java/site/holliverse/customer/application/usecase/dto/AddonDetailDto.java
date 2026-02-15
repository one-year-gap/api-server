package site.holliverse.customer.application.usecase.dto;

import site.holliverse.shared.domain.model.AddonType;

public record AddonDetailDto(
        Long productId,
        AddonType addonType,
        String description
) {}
