package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.AdminRegionalTopPlanDao;
import site.holliverse.admin.query.dao.RegionalSubscriberCountRawData;
import site.holliverse.admin.query.dao.RegionalTopPlanRawData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 전지역 Top3 요금제 요약 조회 유스케이스.
 *
 * 책임:
 * - DAO에서 가져온 "지역별 가입자 수"와 "지역별 Top3 요금제"를
 *   API 응답에 맞는 형태로 조합한다.
 */
@Profile("admin")
@Service
@RequiredArgsConstructor
public class RetrieveRegionalTopPlanUseCase {

    // 지역별 상위 요금제 개수(요구사항 고정값)
    private static final int TOP_PLAN_LIMIT = 3;

    private final AdminRegionalTopPlanDao adminRegionalTopPlanDao;

    /**
     * 전지역 요약 데이터를 한 번에 조회한다.
     *
     * 처리 순서:
     * 1) 지역별 총 가입자 수 조회
     * 2) 지역별 Top3 요금제 조회
     * 3) 지역명을 기준으로 두 결과를 병합
     */
    @Transactional(readOnly = true)
    public List<RegionalTopPlanSummary> execute() {

        // 지역별
        List<RegionalSubscriberCountRawData> regionalCounts =
                adminRegionalTopPlanDao.findSubscriberCountsByAllProvinces();
        List<RegionalTopPlanRawData> regionalTopPlans =
                adminRegionalTopPlanDao.findTopPlansByAllProvinces(TOP_PLAN_LIMIT);

        // 지역별 top plan 목록을 빠르게 조회하기 위한 맵
        // key: province, value: planName 목록
        Map<String, List<String>> plansByProvince = new LinkedHashMap<>();
        for (RegionalTopPlanRawData row : regionalTopPlans) {
            plansByProvince
                    .computeIfAbsent(row.province(), k -> new ArrayList<>())
                    .add(row.planName());
        }

        // 가입자 수 목록을 기준으로 최종 요약 객체를 생성
        List<RegionalTopPlanSummary> summaries = new ArrayList<>();

        for (RegionalSubscriberCountRawData row : regionalCounts) {
            summaries.add(new RegionalTopPlanSummary(
                    row.province(),
                    row.regionalSubscriberCount(),
                    plansByProvince.getOrDefault(row.province(), List.of())
            ));
        }
        return summaries;
    }

    /**
     * 컨트롤러/어셈블러로 전달되는 지역별 요약 데이터.
     */
    public record RegionalTopPlanSummary(

            //지역
            String province,
            //지역 구독자 합계
            long regionalSubscriberCount,
            //탑3 요금제
            List<String> topPlanNames
    ) {
    }
}
