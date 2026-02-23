package site.holliverse.admin.web.assembler;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.application.usecase.RetrieveRegionalTopPlanUseCase;
import site.holliverse.admin.web.dto.analytics.AdminRegionalTopPlanResponseDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Profile("admin")
@Component
public class AdminRegionalTopPlanAssembler {

    // 지도/차트 렌더링 기준 고정 지역 목록
    private static final List<String> REGIONS = List.of(
            "서울특별시",
            "인천광역시",
            "경기도",
            "강원도",
            "충청남도",
            "세종특별자치시",
            "대전광역시",
            "충청북도",
            "경상북도",
            "대구광역시",
            "울산광역시",
            "부산광역시",
            "경상남도",
            "전라북도",
            "광주광역시",
            "전라남도",
            "제주특별자치도"
    );

    /**
     * 변환 규칙:
     * - summary.province -> dto.region
     * - summary.topPlanNames -> dto.topPlans[].planName
     * - 없는 지역은 regionalSubscriberCount=0, topPlans=[]로 채운다.
     */
    public AdminRegionalTopPlanResponseDto toResponse(
            List<RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary> summaries
    ) {
        // 지역명 정규화(공백 제거) 기준으로 빠르게 조회하기 위한 맵
        Map<String, RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary> summaryByRegion = new LinkedHashMap<>();
        for (RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary summary : summaries) {
            summaryByRegion.put(normalize(summary.province()), summary);
        }

        // 고정 지역 순서대로 응답 리스트를 만든다.
        List<AdminRegionalTopPlanResponseDto.RegionTopPlanDto> regions = new ArrayList<>();
        for (String regionName : REGIONS) {
            RetrieveRegionalTopPlanUseCase.RegionalTopPlanSummary summary = summaryByRegion.get(normalize(regionName));

            long regionalSubscriberCount = 0L;
            List<AdminRegionalTopPlanResponseDto.TopPlanDto> topPlans = new ArrayList<>();

            if (summary != null) {
                regionalSubscriberCount = summary.regionalSubscriberCount();
                for (String planName : summary.topPlanNames()) {
                    topPlans.add(new AdminRegionalTopPlanResponseDto.TopPlanDto(planName));
                }
            }

            regions.add(new AdminRegionalTopPlanResponseDto.RegionTopPlanDto(
                    regionName,
                    regionalSubscriberCount,
                    topPlans
            ));
        }

        return new AdminRegionalTopPlanResponseDto(regions);
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }
}
