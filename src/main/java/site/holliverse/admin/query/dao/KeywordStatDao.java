package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import site.holliverse.admin.web.dto.support.KeywordBubbleChartResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static site.holliverse.admin.query.jooq.Tables.BUSINESS_KEYWORD;
import static site.holliverse.admin.query.jooq.Tables.BUSINESS_KEYWORD_MAPPING_RESULT;
import static site.holliverse.admin.query.jooq.Tables.CONSULTATION_ANALYSIS;
import static site.holliverse.admin.query.jooq.Tables.SUPPORT_CASE;
import static site.holliverse.admin.query.jooq.enums.AnalysisStatus.COMPLETED;

@Repository
@RequiredArgsConstructor
public class KeywordStatDao {

    private final DSLContext dsl;

    /**
     * [메인 통계 쿼리]
     * 선택한 년/월의 비즈니스 키워드 누적 빈도수 TOP 10 조회
     * 파라미터가 없으면(null) '전체 기간'의 TOP 10 조회
     *
     * @param year  조회할 년도 (예: 2026)
     * @param month 조회할 월 (예: 3)
     * @return changeRate(증감율)은 일단 null로 채워진 TOP 10 통계 리스트
     */
    public List<KeywordBubbleChartResponseDto> getTop10KeywordStats(Integer year, Integer month) {

        // 1. 동적 날짜 조건 생성 (기본값: 조건 없음 = 전체 기간)
        Condition dateCondition = DSL.noCondition();

        // 프론트엔드에서 year와 month를 넘겨주었다면, 해당 월의 1일 ~ 말일 직전까지로 필터링
        if (year != null && month != null) {
            LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

            dateCondition = SUPPORT_CASE.CREATED_AT.ge(startOfMonth)
                    .and(SUPPORT_CASE.CREATED_AT.lt(endOfMonth));
        }

        // 2. jOOQ 쿼리 실행
        return dsl.select(
                        BUSINESS_KEYWORD.BUSINESS_KEYWORD_ID.as("keywordId"),
                        BUSINESS_KEYWORD.KEYWORD_NAME.as("keywordName"),

                        // 조건에 맞는 기간 동안 해당 키워드가 등장한 총 횟수 합산 (SUM)
                        // 만약 결과가 null이면 0으로 치환(coalesce)하여 NullPointerException 방지
                        DSL.coalesce(DSL.sum(BUSINESS_KEYWORD_MAPPING_RESULT.COUNT), BigDecimal.ZERO)
                                .cast(Integer.class).as("totalCount"),

                        // 증감율은 DB 쿼리에서 알 수 없으므로, DTO 매핑 짝을 맞추기 위해 강제로 null 삽입
                        // (이후 Service 로직에서 지난달 데이터를 가져와 수동으로 계산해 덮어씌움)
                        DSL.inline((BigDecimal) null).as("changeRate")
                )
                .from(BUSINESS_KEYWORD_MAPPING_RESULT)

                // 매핑 결과 테이블에 마스터 키워드 이름을 붙이기 위한 조인
                .join(BUSINESS_KEYWORD)
                .on(BUSINESS_KEYWORD_MAPPING_RESULT.BUSINESS_KEYWORD_ID.eq(BUSINESS_KEYWORD.BUSINESS_KEYWORD_ID))

                // 매핑 결과가 어떤 상담 분석(티켓)에서 나왔는지 찾기 위한 조인
                .join(CONSULTATION_ANALYSIS)
                .on(BUSINESS_KEYWORD_MAPPING_RESULT.ANALYSIS_ID.eq(CONSULTATION_ANALYSIS.ANALYSIS_ID))

                // 상담 분석의 원본 티켓 작성일자(created_at)를 기준으로 필터링하기 위한 조인
                .join(SUPPORT_CASE)
                .on(CONSULTATION_ANALYSIS.CASE_ID.eq(SUPPORT_CASE.CASE_ID))

                // 위에서 동적으로 만든 날짜 조건(전체 or 특정 월) 적용
                .where(dateCondition)

                // 키워드의 ID와 이름별로 묶어서 합계를 구함
                .groupBy(BUSINESS_KEYWORD.BUSINESS_KEYWORD_ID, BUSINESS_KEYWORD.KEYWORD_NAME)

                // 가장 많이 등장한 키워드 순으로 내림차순 정렬 (1등부터 줄 세우기)
                .orderBy(DSL.sum(BUSINESS_KEYWORD_MAPPING_RESULT.COUNT).desc())

                // 상위 10개만
                .limit(10)

                // 레코드 형태를 DTO 클래스에 정확히 꽂아서 반환
                .fetchInto(KeywordBubbleChartResponseDto.class);
    }

    /**
     * [서포트 쿼리]
     * Service 계층에서 "지난달 대비 증감율"을 계산할 때 사용하기 위해,
     * 특정 키워드 리스트(TOP 10)의 "특정 년/월(보통 지난달)" 빈도수를 가져오는 쿼리
     */
    public List<KeywordBubbleChartResponseDto> getKeywordStatsByIds(List<Long> keywordIds, int year, int month) {
        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        return dsl.select(
                        BUSINESS_KEYWORD.BUSINESS_KEYWORD_ID.as("keywordId"),
                        BUSINESS_KEYWORD.KEYWORD_NAME.as("keywordName"),
                        DSL.coalesce(DSL.sum(BUSINESS_KEYWORD_MAPPING_RESULT.COUNT), BigDecimal.ZERO)
                                .cast(Integer.class).as("totalCount"),
                        DSL.inline((BigDecimal) null).as("changeRate")
                )
                .from(BUSINESS_KEYWORD_MAPPING_RESULT)
                .join(BUSINESS_KEYWORD).on(BUSINESS_KEYWORD_MAPPING_RESULT.BUSINESS_KEYWORD_ID.eq(BUSINESS_KEYWORD.BUSINESS_KEYWORD_ID))
                .join(CONSULTATION_ANALYSIS).on(BUSINESS_KEYWORD_MAPPING_RESULT.ANALYSIS_ID.eq(CONSULTATION_ANALYSIS.ANALYSIS_ID))
                .join(SUPPORT_CASE).on(CONSULTATION_ANALYSIS.CASE_ID.eq(SUPPORT_CASE.CASE_ID))
                .where(SUPPORT_CASE.CREATED_AT.ge(startOfMonth)
                        .and(SUPPORT_CASE.CREATED_AT.lt(endOfMonth))
                        // 넘어온 TOP 10 키워드 ID 리스트에 포함된 것만 가져오기 (IN 절)
                        .and(BUSINESS_KEYWORD.BUSINESS_KEYWORD_ID.in(keywordIds)))
                .groupBy(BUSINESS_KEYWORD.BUSINESS_KEYWORD_ID, BUSINESS_KEYWORD.KEYWORD_NAME)
                .fetchInto(KeywordBubbleChartResponseDto.class);
    }

    /**
     * [배치 이력 확인용]
     * 상태가 'COMPLETED'인 분석 내역 중, 가장 최신의 원본 상담 생성 일자(CREATED_AT)를 가져온다
     */
    public LocalDate getLastAnalyzedCaseDate() {
        LocalDateTime maxDateTime = dsl.select(DSL.max(SUPPORT_CASE.CREATED_AT))
                .from(CONSULTATION_ANALYSIS)
                .join(SUPPORT_CASE).on(CONSULTATION_ANALYSIS.CASE_ID.eq(SUPPORT_CASE.CASE_ID))
                .where(CONSULTATION_ANALYSIS.ANALYSIS_STATUS.eq(COMPLETED))
                .fetchOneInto(LocalDateTime.class);

        // 데이터가 아예 없으면 null 반환, 있으면 날짜(LocalDate)만 잘라서 반환
        return maxDateTime != null ? maxDateTime.toLocalDate() : null;
    }
}