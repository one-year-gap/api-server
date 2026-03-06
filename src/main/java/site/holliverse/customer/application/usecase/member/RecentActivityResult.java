package site.holliverse.customer.application.usecase.member;

import java.time.OffsetDateTime;
import java.util.List;

public record RecentActivityResult(
        List<ActivityItem> items
) {

    public record ActivityItem(
            Long productId,
            String productName,
            String productType,
            List<String> tags,
            OffsetDateTime viewedAt
    ) {
    }
}

