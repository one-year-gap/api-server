package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.query.dao.KeywordStatDao;
import site.holliverse.admin.web.dto.support.KeywordBubbleChartResponseDto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetKeywordBubbleChartUseCaseTest {

    @Mock
    private KeywordStatDao keywordStatDao;

    @InjectMocks
    private GetKeywordBubbleChartUseCase getKeywordBubbleChartUseCase;

    @Test
    @DisplayName("전체 기간 조회 시(year, month가 null) 증감율 계산 없이 메인 통계만 반환한다.")
    void execute_overallStats_returnsWithoutCalculation() {
        // given
        List<KeywordBubbleChartResponseDto> mockStats = List.of(
                new KeywordBubbleChartResponseDto(1L, "모바일", 100, null)
        );
        given(keywordStatDao.getTop10KeywordStats(null, null)).willReturn(mockStats);

        // when
        List<KeywordBubbleChartResponseDto> result = getKeywordBubbleChartUseCase.execute(null, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).changeRate()).isNull(); // 증감율 계산 로직을 타지 않음
        verify(keywordStatDao).getTop10KeywordStats(null, null);
    }

    @Test
    @DisplayName("조회된 데이터가 없으면 빈 리스트를 반환한다.")
    void execute_noData_returnsEmptyList() {
        // given
        given(keywordStatDao.getTop10KeywordStats(2026, 3)).willReturn(Collections.emptyList());

        // when
        List<KeywordBubbleChartResponseDto> result = getKeywordBubbleChartUseCase.execute(2026, 3);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("정상적인 월별 조회 시 전월 대비 증감율을 정확히 계산한다.")
    void execute_monthlyStats_calculatesChangeRate() {
        // given (3월 데이터: 모바일 100건, TV 50건)
        List<KeywordBubbleChartResponseDto> currentStats = List.of(
                new KeywordBubbleChartResponseDto(1L, "모바일", 100, null),
                new KeywordBubbleChartResponseDto(2L, "TV", 50, null),
                new KeywordBubbleChartResponseDto(3L, "신규키워드", 10, null)
        );

        // given (2월 데이터: 모바일 50건[증가], TV 100건[감소], 신규키워드 없음[신규])
        List<KeywordBubbleChartResponseDto> previousStats = List.of(
                new KeywordBubbleChartResponseDto(1L, "모바일", 50, null),
                new KeywordBubbleChartResponseDto(2L, "TV", 100, null)
        );

        given(keywordStatDao.getTop10KeywordStats(2026, 3)).willReturn(currentStats);
        given(keywordStatDao.getKeywordStatsByIds(anyList(), eq(2026), eq(2))).willReturn(previousStats);

        // when
        List<KeywordBubbleChartResponseDto> result = getKeywordBubbleChartUseCase.execute(2026, 3);

        // then
        // 1. 모바일: ((100 - 50) / 50) * 100 = 100.00%
        assertThat(result.get(0).keywordName()).isEqualTo("모바일");
        assertThat(result.get(0).changeRate()).isEqualByComparingTo(new BigDecimal("100.00"));

        // 2. TV: ((50 - 100) / 100) * 100 = -50.00%
        assertThat(result.get(1).keywordName()).isEqualTo("TV");
        assertThat(result.get(1).changeRate()).isEqualByComparingTo(new BigDecimal("-50.00"));

        // 3. 신규키워드: 지난달 0건 -> 이번달 10건 = 100.00% (방어 로직)
        assertThat(result.get(2).keywordName()).isEqualTo("신규키워드");
        assertThat(result.get(2).changeRate()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}