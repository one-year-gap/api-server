package site.holliverse.admin.web.dto.churn;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record ChurnRealTimeResponseDto(
        List<Item> items,
        Long afterId,
        boolean hasMore
) {
    @Builder
    public record Item(
            Long churnId,
            Long memberId,
            String reason,
            String churnLevel,
            String memberName,
            OffsetDateTime timeStamp
    ) {
    }
}
