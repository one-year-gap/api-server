package site.holliverse.customer.web.dto.persona;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * /persona-types/me 응답 DTO.
 */
public record PersonaDetailResponse(
        Long personaTypeId, //페르소나
        String characterName,
        String shortDesc,
        String characterDescription,
        Integer version,
        Boolean isActive,
        List<String> tags,
        TscoreIndex tscoreIndex
) {
    /** 화면 표시용 T-score 지수 묶음. */
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
