package site.holliverse.customer.application.usecase.dto;

import java.util.List;

/**
 * 요금제 비교 시 혜택/스펙 항목별 변경 내용 (Application 계층).
 */
public record BenefitChangeItemDto(
        String item,
        boolean isChanged,
        String desc,
        List<String> addedBrands,
        List<String> removedBrands
) {
    public static BenefitChangeItemDto of(String item, boolean isChanged, String desc) {
        return new BenefitChangeItemDto(item, isChanged, desc, List.of(), List.of());
    }

    public static BenefitChangeItemDto of(String item, boolean isChanged, String desc,
                                          List<String> addedBrands, List<String> removedBrands) {
        return new BenefitChangeItemDto(
                item, isChanged, desc,
                addedBrands != null ? addedBrands : List.of(),
                removedBrands != null ? removedBrands : List.of());
    }
}
