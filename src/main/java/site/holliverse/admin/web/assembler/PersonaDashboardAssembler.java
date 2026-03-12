package site.holliverse.admin.web.assembler;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.query.dao.PersonaDistributionData;
import site.holliverse.admin.query.dao.PersonaMonthlyTrendData;
import site.holliverse.admin.web.dto.analytics.PersonaDistributionResponseDto;
import site.holliverse.admin.web.dto.analytics.PersonaMonthlyTrendResponseDto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 페르소나 대시보드 통계 데이터 조립
 */
@Profile("admin")
@Component
public class PersonaDashboardAssembler {

    /**
     * DAO Data -> Web Response 변환 및 퍼센트 계산
     */
    public List<PersonaDistributionResponseDto> toDistributionResponses(List<PersonaDistributionData> rawDataList) {
        if (rawDataList == null || rawDataList.isEmpty()) {
            return List.of();
        }

        // 1. 전체 유저 수 합산 (퍼센트 계산의 분모 역할)
        long totalUsers = rawDataList.stream()
                .mapToLong(PersonaDistributionData::userCount)
                .sum();

        // 2. 응답 DTO로 매핑하며 퍼센트 계산
        return rawDataList.stream()
                .map(data -> {
                    // 소수점 첫째 자리까지 반올림 (예: 33.3)
                    double percentage = totalUsers == 0 ? 0.0 :
                            Math.round(((double) data.userCount() / totalUsers) * 1000) / 10.0;

                    return new PersonaDistributionResponseDto(
                            data.personaName(),
                            data.userCount(),
                            percentage,
                            data.top3PlanNames()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 월별 트렌드 데이터 리스트 단순 매핑
     */
    public List<PersonaMonthlyTrendResponseDto> toMonthlyTrendResponses(List<PersonaMonthlyTrendData> rawDataList) {
        if (rawDataList == null || rawDataList.isEmpty()) {
            return List.of();
        }

        return rawDataList.stream()
                .map(data -> new PersonaMonthlyTrendResponseDto(
                        data.yearMonth(),
                        data.personaName(),
                        data.userCount()
                ))
                .collect(Collectors.toList());
    }
}