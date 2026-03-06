package site.holliverse.customer.web.mapper;

import site.holliverse.customer.application.usecase.member.RecentActivityResult;
import site.holliverse.customer.web.dto.member.RecentActivityResponse;

import java.util.List;

public class RecentActivityResponseMapper {

    public RecentActivityResponse toResponse(RecentActivityResult result) {
        List<RecentActivityResponse.ActivityItem> items = result.items().stream()
                .map(item -> new RecentActivityResponse.ActivityItem(
                        item.productId(),
                        item.productName(),
                        item.productType(),
                        item.tags(),
                        item.viewedAt()
                ))
                .toList();

        return new RecentActivityResponse(items);
    }
}

