package site.holliverse.admin.domain.model.churn;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이탈률 위험도 등급.
 */
@Getter
@RequiredArgsConstructor
public enum ChurnRiskGrade {
    LOW("보통"),
    MEDIUM("경고"),
    HIGH("위험");

    private final String label;
}
