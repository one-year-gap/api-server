package site.holliverse.customer.web.dto.compare;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 요금제 비교 시 혜택/스펙 항목별 변경 내용.
 */
public record BenefitChangeItem(
        String item,
        @JsonProperty("is_changed") boolean isChanged,
        String desc,
        @JsonProperty("added_brands") List<String> addedBrands,
        @JsonProperty("removed_brands") List<String> removedBrands
) {
    /** 문자열 비교용 (added/removed 없음) */
    public static BenefitChangeItem of(String item, boolean isChanged, String desc) {
        return new BenefitChangeItem(item, isChanged, desc, List.of(), List.of());
    }

    /** Set 파싱 혜택용 (added/removed 포함) */
    public static BenefitChangeItem of(String item, boolean isChanged, String desc,
                                       List<String> addedBrands, List<String> removedBrands) {
        return new BenefitChangeItem(item, isChanged, desc,
                addedBrands != null ? addedBrands : List.of(),
                removedBrands != null ? removedBrands : List.of());
    }
}
