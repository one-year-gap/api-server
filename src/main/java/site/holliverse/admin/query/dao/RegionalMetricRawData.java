package site.holliverse.admin.query.dao;

import java.math.BigDecimal;

public record RegionalMetricRawData(
        String province,
        BigDecimal avgSales,
        BigDecimal avgDataUsageGb
) {
}
