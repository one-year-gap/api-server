package site.holliverse.admin.web.dto.churn;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record ChurnRiskMemberListRequestDto(
        @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
        Integer page,
        @Min(value = 1, message = "사이즈는 1 이상이어야 합니다.")
        Integer size,
        String keyword,
        List<@Pattern(
                regexp = "^(VVIP|VIP|GOLD)$",
                message = "유효하지 않은 멤버십 등급입니다."
        ) String> memberships,
        List<@Pattern(
                regexp = "^(HIGH|MEDIUM)$",
                message = "유효하지 않은 위험도입니다."
        ) String> riskLevels
) {
    public ChurnRiskMemberListRequestDto {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        if (keyword != null) {
            keyword = keyword.replace("-", "");
        }
    }

    public int getOffset() {
        return (page - 1) * size;
    }
}
