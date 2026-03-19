package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.ChurnRealtimeDao;
import site.holliverse.admin.query.dao.ChurnRealtimeRawData;
import site.holliverse.admin.web.dto.churn.ChurnRealTimeRequestDto;

import java.util.Collections;
import java.util.List;

@Profile("admin")
@Service
@RequiredArgsConstructor
public class RetrieveChurnRealtimeUseCase {
    private final ChurnRealtimeDao churnRealtimeDao;

    /**
     * 초기 대시보드 적재용 최신 목록 조회
     */
    @Transactional(readOnly = true)
    public RetrieveChurnRealtimeResult latest(ChurnRealTimeRequestDto requestDto) {
        List<ChurnRealtimeRawData> rows = churnRealtimeDao.findLatest(requestDto);
        return sliceLatest(rows, requestDto.normalizedLimit());
    }

    /**
     * 마지막 커서 이후 변경분 조회
     */
    @Transactional(readOnly = true)
    public RetrieveChurnRealtimeResult changes(ChurnRealTimeRequestDto requestDto) {
        List<ChurnRealtimeRawData> rows = churnRealtimeDao.findChanges(requestDto);
        return sliceChanges(rows, requestDto.normalizedLimit());
    }

    /**
     * 최신순 조회 결과에서 한 페이지 분량과 다음 커서
     */
    private RetrieveChurnRealtimeResult sliceLatest(List<ChurnRealtimeRawData> rows, int limit) {
        if (rows.isEmpty()) {
            return new RetrieveChurnRealtimeResult(Collections.emptyList(), 0L, false);
        }

        boolean hasMore = rows.size() > limit;
        List<ChurnRealtimeRawData> items = hasMore ? rows.subList(0, limit) : rows;
        long afterId = items.stream()
                .map(ChurnRealtimeRawData::getChurnId)
                .max(Long::compareTo)
                .orElse(0L);

        return new RetrieveChurnRealtimeResult(items, afterId, hasMore);
    }

    /**
     * 변경분은 DB에서 오래된 순으로 읽고, 응답 직전에 최신순
     */
    private RetrieveChurnRealtimeResult sliceChanges(List<ChurnRealtimeRawData> rows, int limit) {
        if (rows.isEmpty()) {
            return new RetrieveChurnRealtimeResult(Collections.emptyList(), 0L, false);
        }

        boolean hasMore = rows.size() > limit;
        List<ChurnRealtimeRawData> ascItems = hasMore ? rows.subList(0, limit) : rows;
        long afterId = ascItems.stream()
                .map(ChurnRealtimeRawData::getChurnId)
                .max(Long::compareTo)
                .orElse(0L);

        List<ChurnRealtimeRawData> descItems = new java.util.ArrayList<>(ascItems);
        Collections.reverse(descItems);

        return new RetrieveChurnRealtimeResult(descItems, afterId, hasMore);
    }

    public record RetrieveChurnRealtimeResult(
            List<ChurnRealtimeRawData> items,
            long afterId,
            boolean hasMore
    ) {
    }
}
