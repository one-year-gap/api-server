package site.holliverse.admin.web.assembler;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.application.usecase.RetrieveRegionalTopPlanUseCase;
import site.holliverse.admin.web.dto.analytics.AdminRegionalTopPlanResponseDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static site.holliverse.admin.web.dto.analytics.AdminRegionalTopPlanResponseDto.*;

@Profile("admin")
@Component
public class AdminRegionalTopPlanAssembler {

    // 지역 코드 기준표.
    // key   : 응답에 내려줄 표준 지역명
    // value : 고정 지역 코드
    // 데이터 유무와 관계없이 동일 코드가 내려가도록 기준값을 고정한다.
    private static final Map<String, String> REGION_CODE_MAP;

    // 응답 지역 순서를 고정하기 위한 목록.
    private static final List<String> REGIONS;

    /**
     * 기준표는 비즈니스 상수이므로 static final로 선언하고,
     * Map/List를 불변 형태로 감싸 외부 수정을 차단한다.
     */
    static {
        // LinkedHashMap을 사용해 삽입 순서(=응답 순서)를 보장한다.
        Map<String, String> map = new LinkedHashMap<>();
        map.put("서울특별시", "R001");
        map.put("인천광역시", "R002");
        map.put("경기도", "R003");
        map.put("강원도", "R004");
        map.put("충청남도", "R005");
        map.put("세종특별자치시", "R006");
        map.put("대전광역시", "R007");
        map.put("충청북도", "R008");
        map.put("경상북도", "R009");
        map.put("대구광역시", "R010");
        map.put("울산광역시", "R011");
        map.put("부산광역시", "R012");
        map.put("경상남도", "R013");
        map.put("전라북도", "R014");
        map.put("광주광역시", "R015");
        map.put("전라남도", "R016");
        map.put("제주특별자치도", "R017");

        REGION_CODE_MAP = Map.copyOf(map);
        REGIONS = List.copyOf(map.keySet());
    }

    /**
     * 지역별 Top3 요금제 응답을 구성한다.
     * @param summaries 에는 지역명, 해당지역 가입자수, 해당지역 Top요금제 이름 리스트가 존재
     *
     * 처리 규칙:
     * 1) 사전 정의된 모든 지역을 항상 응답에 포함한다.
     * 2) 지역 데이터가 없으면 가입자 수 0, topPlans 빈 배열로 채운다.
     * 3) regionCode는 고정 매핑(REGION_CODE_MAP)으로 반환한다.
     */
    public AdminRegionalTopPlanResponseDto toResponse(
            List<RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary> summaries
    ) {
        // 조회 결과를 정규화 지역명 기준으로 빠르게 찾기 위한 맵으로 변환한다.
        // 지역, 지역 가입자, top3 구독자
        Map<String, RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary> summaryByRegion = new LinkedHashMap<>();
        for (RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary summary : summaries) {
            summaryByRegion.put(normalize(summary.province()), summary);
        }

        //RegionTopPlanDto : regions들,  지역코드, 지역 이름, 지역 가입자 수, 3개 상위 계획
        List<RegionTopPlanDto> regions = new ArrayList<>();

        //지역 고정 목록들을 하나씩 돈다.
        for (String regionName : REGIONS) {
            RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary summary = summaryByRegion.get(normalize(regionName));

            // 지역 기본값으로 0을 넣은뒤
            long regionalSubscriberCount = 0L;
            List<TopPlanDto> topPlans = new ArrayList<>();

            //해당 지역의 summary가 있으면 실제 값으로 다시 덮어 쓴다.
            if (summary != null) {
                regionalSubscriberCount = summary.regionalSubscriberCount();
                for (String planName : summary.topPlanNames()) {
                    topPlans.add(new TopPlanDto(planName));
                }
            }

            //최종적으로 dto 만들어 regions에 추가하낟.
            regions.add(new RegionTopPlanDto(
                    toRegionCode(regionName),
                    regionName,
                    regionalSubscriberCount,
                    topPlans
            ));
        }

        return new AdminRegionalTopPlanResponseDto(regions);
    }

    // 공백 차이로 인한 매칭 실패를 막기 위해 지역명을 정규화한다.
    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }

    // 고정 지역코드를 반환한다. 예외 지역명은 R000 처리.
    private String toRegionCode(String region) {
        return REGION_CODE_MAP.getOrDefault(region, "R000");
    }
}