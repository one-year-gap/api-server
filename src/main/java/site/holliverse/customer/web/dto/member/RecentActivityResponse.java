package site.holliverse.customer.web.dto.member;

import java.time.OffsetDateTime;
import java.util.List;

public record RecentActivityResponse(
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

