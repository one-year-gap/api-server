package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.PersonaDashboardDao;
import site.holliverse.admin.query.dao.PersonaDistributionData;
import site.holliverse.admin.query.dao.PersonaMonthlyTrendData;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 대시보드 - 페르소나 통계 조회를 담당하는 UseCase
 */
@Profile("admin")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPersonaDashboardUseCase {

    private final PersonaDashboardDao personaDashboardDao;

    /**
     * 1. 페르소나별 분포도 및 Top 3 모바일 요금제 조회
     * * @return 페르소나 분포 데이터 리스트 (DAO DTO 반환 -> Web 계층에서 응답 DTO로 조립 예정)
     */
    public List<PersonaDistributionData> getDistribution() {
        // 기준일: 대시보드 접속 시점의 날짜
        LocalDate targetDate = LocalDate.now();

        return personaDashboardDao.findDistributionAndTopPlansByDate(targetDate);
    }

    /**
     * 2. 최근 5개월간의 페르소나별 월별 사용자 수 트렌드 조회
     * * @return 월별 페르소나 트렌드 데이터 리스트
     */
    public List<PersonaMonthlyTrendData> getMonthlyTrend() {
        LocalDate endDate = LocalDate.now();

        // 기준 시작일: 최근 5개월 치 (이번 달 포함)
        LocalDate startDate = endDate.minusMonths(4).withDayOfMonth(1);

        return personaDashboardDao.findMonthlyTrendByPeriod(startDate, endDate);
    }
}