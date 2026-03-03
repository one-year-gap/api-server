package site.holliverse.admin.web.dto.member;

import java.math.BigDecimal;

public record TotalMembershipResponseDto(
        BigDecimal totalInK,
        BigDecimal vvipRate,
        BigDecimal vipRate,
        BigDecimal goldRate
) {
}
