package site.holliverse.customer.application.usecase.persona;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 유스케이스 내부 반환 모델.
 * 웹 응답 DTO로 변환되기 전 도메인 데이터를 담는다.
 */
public record PersonaDetailResult(
        Long personaTypeId,
        String characterName,
        String shortDesc,
        String characterDescription,
        Integer version,
        Boolean isActive,
        List<String> tags,
        TscoreIndex tscoreIndex,
        boolean isDefault
) {
    /** 회원의 최신 T-score 스냅샷 6개 지수. */
    public record TscoreIndex(
            LocalDate snapshotDate,
            BigDecimal exploreTscore,
            BigDecimal benefitTrendTscore,
            BigDecimal multiDeviceTscore,
            BigDecimal familyHomeTscore,
            BigDecimal internetSecurityTscore,
            BigDecimal stabilityTscore
    ) {
    }
}
