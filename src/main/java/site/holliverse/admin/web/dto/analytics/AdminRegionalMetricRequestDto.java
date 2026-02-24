package site.holliverse.admin.web.dto.analytics;

import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**==========================
 * 년월을 입력받는 request dto
 * 입력값이 주어지지 않았을 경우 현재 년월을 자동으로 설정한다.
 * @author nonstop
 * @version 1.0.0
 * @since 2026-02-23
 * ========================== */
public record AdminRegionalMetricRequestDto(
        String yyyymm
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    public AdminRegionalMetricRequestDto(String yyyymm) {
        if (yyyymm == null || yyyymm.isBlank()) {
            yyyymm = YearMonth.now().format(FORMATTER);
        }

        if (!yyyymm.matches("\\d{6}")) {
            throw new CustomException(
                    ErrorCode.INVALID_INPUT,
                    "yyyymm"
            );
        }

        int month = Integer.parseInt(yyyymm.substring(4, 6));
        if (month < 1 || month > 12) {
            throw new CustomException(
                    ErrorCode.INVALID_INPUT,
                    "yyyymm"
            );
        }
        this.yyyymm = yyyymm;
    }
}
