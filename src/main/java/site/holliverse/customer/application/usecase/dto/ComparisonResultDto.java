package site.holliverse.customer.application.usecase.dto;

import java.util.List;

/**
 * 요금제 비교 결과 (가격 차이 + 필드별 변경 내역).
 */
public record ComparisonResultDto(
        int priceDiff,
        String message,
        List<BenefitChangeItemDto> benefitChanges
) {}
