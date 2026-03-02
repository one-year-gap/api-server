package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.KeywordStatDao;
import site.holliverse.admin.web.dto.support.KeywordBubbleChartResponseDto;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetKeywordBubbleChartUseCase {

    private final KeywordStatDao keywordStatDao;

    /**
     * 버블 차트용 통계 데이터를 조립하고 증감율을 계산하여 반환
     */
    public List<KeywordBubbleChartResponseDto> execute(Integer year, Integer month) {

        // 배치 완료 여부 검증 (미래 날짜 방어)
        if (year != null && month != null) {
            LocalDate requestedDate = LocalDate.of(year, month, 1); // 요청한 달의 1일
            LocalDate lastAnalyzedDate = keywordStatDao.getLastAnalyzedCaseDate();

            // DB에 분석된 데이터가 아예 없거나, 요청한 달(1일)이 마지막 분석 날짜보다 미래인 경우
            if (lastAnalyzedDate == null || requestedDate.isAfter(lastAnalyzedDate)) {
                // 400 에러로 처리
                throw new CustomException(ErrorCode.DATA_NOT_YET_ANALYZED);
            }
        }

        // 1. 메인 쿼리: 이번 달(또는 전체 기간) TOP 10 키워드 조회
        List<KeywordBubbleChartResponseDto> currentStats = keywordStatDao.getTop10KeywordStats(year, month);

        // 2. 전체 기간 조회(year, month가 null)이거나, 조회된 데이터가 없으면 증감율 계산 없이 바로 반환
        if (year == null || month == null || currentStats.isEmpty()) {
            return currentStats;
        }

        // 3. 지난달 날짜(년/월) 계산
        LocalDate currentDate = LocalDate.of(year, month, 1);
        LocalDate previousDate = currentDate.minusMonths(1);
        int prevYear = previousDate.getYear();
        int prevMonth = previousDate.getMonthValue();

        // 4. 추출 로직: 이번 달 TOP 10 키워드의 ID(PK)만 리스트로 가져오기
        List<Long> top10KeywordIds = currentStats.stream()
                .map(KeywordBubbleChartResponseDto::keywordId)
                .toList();

        // 5. 서포트 쿼리: TOP 10 키워드들의 지난달 빈도수 조회
        // 빠른 검색을 위해 List를 Map 구조로 변환 (Key: 키워드 ID, Value: 총 등장 횟수)
        Map<Long, Integer> previousStatsMap = keywordStatDao.getKeywordStatsByIds(top10KeywordIds, prevYear, prevMonth)
                .stream()
                .collect(Collectors.toMap(
                        KeywordBubbleChartResponseDto::keywordId,
                        KeywordBubbleChartResponseDto::totalCount
                ));

        // 6. 증감율 계산 및 새로운 DTO 래핑
        return currentStats.stream().map(current -> {
            int currentCount = current.totalCount();
            // Map에서 지난달 횟수를 꺼내옴 (지난달에 한 번도 안 나왔다면 기본값 0)
            int previousCount = previousStatsMap.getOrDefault(current.keywordId(), 0);

            // 증감율 공식 적용
            BigDecimal changeRate = calculateChangeRate(currentCount, previousCount);

            // changeRate가 채워진 새로운 DTO 생성 및 반환
            return new KeywordBubbleChartResponseDto(
                    current.keywordId(),
                    current.keywordName(),
                    currentCount,
                    changeRate
            );
        }).toList();
    }

    /**
     * 증감율(%) 계산 공식 ((이번 달 - 지난달) / 지난달) * 100
     */
    private BigDecimal calculateChangeRate(int currentCount, int previousCount) {
        // 방어 로직: 지난달에 0건이었다면? (0으로 나누면 에러 발생)
        if (previousCount == 0) {
            // 이번 달에 새로 등장했다면 +100.00%
            return currentCount > 0 ? new BigDecimal("100.00") : BigDecimal.ZERO;
        }

        double rate = ((double) (currentCount - previousCount) / previousCount) * 100;

        // 소수점 둘째 자리까지 반올림
        return BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP);
    }
}