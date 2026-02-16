package site.holliverse.customer.web.dto.compare;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 요금제 비교 결과 (가격 차이 + 필드별 변경 내역).
 */
public record ComparisonResponse(
        @JsonProperty("price_diff") int priceDiff,
        String message,
        @JsonProperty("benefit_changes") List<BenefitChangeItem> benefitChanges
) {}
