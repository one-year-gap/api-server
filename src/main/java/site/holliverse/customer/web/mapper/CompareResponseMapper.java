package site.holliverse.customer.web.mapper;

import site.holliverse.customer.application.usecase.compare.BenefitChangeItemDto;
import site.holliverse.customer.application.usecase.compare.ComparisonResultDto;
import site.holliverse.customer.web.dto.product.compare.BenefitChangeItem;
import site.holliverse.customer.web.dto.product.compare.ComparisonResponse;

import java.util.List;

/**
 * 요금제 비교 결과(ComparisonResultDto)를 API 응답(ComparisonResponse)으로 변환.
 */
public class CompareResponseMapper {

    public ComparisonResponse toComparisonResponse(ComparisonResultDto dto) {
        List<BenefitChangeItem> items = dto.benefitChanges().stream()
                .map(this::toBenefitChangeItem)
                .toList();
        return new ComparisonResponse(dto.priceDiff(), dto.message(), items);
    }

    public BenefitChangeItem toBenefitChangeItem(BenefitChangeItemDto dto) {
        return new BenefitChangeItem(
                dto.item(),
                dto.isChanged(),
                dto.desc(),
                dto.addedBrands(),
                dto.removedBrands()
        );
    }
}
