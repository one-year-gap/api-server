package site.holliverse.admin.web.dto.support;

import java.math.BigDecimal;

public record KeywordBubbleChartResponseDto(
        Long keywordId,             // 마스터 키워드 ID (예: 1)
        String keywordName,         // 마스터 키워드 이름 (예: "모바일")
        Integer totalCount,         // 월간 누적 등장 횟수
        BigDecimal changeRate       // 월간 평균 증감율
) {
}
