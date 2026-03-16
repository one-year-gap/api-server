package site.holliverse.admin.web.dto.churn;

import lombok.Builder;

@Builder
public record ChurnRiskMemberDto(
        int no,
        Long memberId,
        String membership,
        String name,
        String riskLevel,
        String riskReason,
        Integer churnScore,
        String phone,
        String email
) {
}
