package site.holliverse.admin.web.dto.analytics;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public record AdminRegionalMetricRequestDto(
        String yyyymm
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    public AdminRegionalMetricRequestDto {
        if (yyyymm == null || yyyymm.isBlank()) {
            yyyymm = YearMonth.now().format(FORMATTER);
        }

        if (!yyyymm.matches("\\d{6}")) {
            throw new IllegalArgumentException("yyyymm must be formatted as yyyyMM.");
        }

        int month = Integer.parseInt(yyyymm.substring(4, 6));
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("yyyymm month must be between 01 and 12.");
        }
    }
}
