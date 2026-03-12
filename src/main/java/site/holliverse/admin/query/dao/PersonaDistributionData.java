package site.holliverse.admin.query.dao;

import java.util.List;

/**
 * 페르소나 유형별 분포 및 Top 3 요금제 조회 결과용 DTO
 */
public record PersonaDistributionData(
        String personaName,         // 페르소나 이름 (예: SPACE_SHERLOCK)
        long userCount,             // 해당 페르소나에 속한 유저 수
        List<String> top3PlanNames  // 이 그룹이 가장 많이 구독 중인 요금제 3개 리스트
) {
}