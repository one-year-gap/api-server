package site.holliverse.customer.application.usecase.dto;

import site.holliverse.customer.persistence.entity.AddonType;

public record AddonDetailDto(
        Long productId,
        AddonType addonType,
        String description
) {}
