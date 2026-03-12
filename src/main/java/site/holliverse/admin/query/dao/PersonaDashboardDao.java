package site.holliverse.admin.query.dao;

import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 대시보드 - 페르소나 관련 통계 조회를 위한 Port(인터페이스)
 */
@Profile("admin")
public interface PersonaDashboardDao {

    /**
     * 특정 날짜(최신 스냅샷 기준)의 페르소나별 유저 수 및 Top 3 요금제 조회
     *
     * @param targetDate 기준 날짜 (가장 최근 스냅샷 날짜)
     * @return 페르소나별 분포 데이터 목록
     */
    List<PersonaDistributionData> findDistributionAndTopPlansByDate(LocalDate targetDate);

    /**
     * 특정 기간 동안의 매월 페르소나별 사용자 수 트렌드를 조회
     * (매월 말일 스냅샷만 필터링하여 집계)
     *
     * @param startDate 조회 시작일
     * @param endDate   조회 종료일
     * @return 월별 페르소나 트렌드 데이터 목록
     */
    List<PersonaMonthlyTrendData> findMonthlyTrendByPeriod(LocalDate startDate, LocalDate endDate);

    /**
     * 페르소나 스냅샷 테이블에 적재된 가장 최근(MAX) 날짜 조회
     */
    LocalDate findLatestSnapshotDate();
}