package site.holliverse.admin.web.assembler;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.query.dao.RegionalMetricRawData;
import site.holliverse.admin.web.dto.analytics.AdminRegionalMetricResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Profile("admin")
@Component
public class AdminRegionalMetricAssembler {

    /**
     * 표/차트 렌더링 순서를 고정하기 위한 표준 지역 목록.
     * - DB 결과에 일부 지역이 빠져도 응답은 항상 이 순서/개수(17개)를 유지한다.
     * - 프론트가 인덱스 기반으로 series를 묶는 경우에도 깨지지 않는다.
     */
    private static final List<String> REGIONS = List.of(
            "서울특별시",
            "인천광역시",
            "경기도",
            "강원도",
            "충청남도",
            "세종특별자치시",
            "대전광역시",
            "충청북도",
            "경상북도",
            "대구광역시",
            "울산광역시",
            "부산광역시",
            "경상남도",
            "전라북도",
            "광주광역시",
            "전라남도",
            "제주특별자치도"
    );

    /**
     *
     * DAO raw 데이터를 화면 응답 DTO로 변환한다.
     *
     * 처리 순서:
     * 1) raw 지역명을 normalize(공백 제거)해서 맵으로 만든다.
     * 2) REGIONS 17개를 순회하며 각 지역 값을 꺼내 DTO row를 만든다.
     *    - 값이 없으면 0으로 채운다(누락 지역 보정).
     * 3) row를 한 번 더 순회하여 최대 매출/최대 데이터 지역을 찾는다.
     * 4) 최대값을 축 범위용으로 자리수 올림해 axisMax에 담는다.
     */
    public AdminRegionalMetricResponseDto toResponse(List<RegionalMetricRawData> rawData) {
        // DB 집계 결과를 "정규화된 지역명 -> raw row" 맵으로 구성
        Map<String, RegionalMetricRawData> normalizedMap = toNormalizedMap(rawData);

        // 최대값 추적 변수(차트 스케일/강조 영역 계산용)
        long maxSales = 0L;
        long maxDataUsageGb = 0L;
        String maxSalesRegion = REGIONS.get(0);
        String maxDataUsageRegion = REGIONS.get(0);

        List<AdminRegionalMetricResponseDto.RegionMetricDto> regions = REGIONS.stream()
                .map(region -> {
                    // "부산 광역시" vs "부산광역시" 같은 표기 차이를 흡수해서 조회
                    RegionalMetricRawData data = normalizedMap.get(normalize(region));

                    // 누락 지역은 0으로 채워 항상 17개 지역이 응답되도록 보장
                    long avgSales = toLong(data == null ? null : data.avgSales());
                    long avgDataUsage = toLong(data == null ? null : data.avgDataUsageGb());

                    return new AdminRegionalMetricResponseDto.RegionMetricDto(region, avgSales, avgDataUsage);
                })
                .toList();

        // 응답 row 기준으로 최대 매출 지역/최대 데이터 사용 지역 계산
        for (AdminRegionalMetricResponseDto.RegionMetricDto row : regions) {
            if (row.averageSales() > maxSales) {
                maxSales = row.averageSales();
                maxSalesRegion = row.region();
            }
            if (row.averageDataUsageGb() > maxDataUsageGb) {
                maxDataUsageGb = row.averageDataUsageGb();
                maxDataUsageRegion = row.region();
            }
        }

        return new AdminRegionalMetricResponseDto(
                regions,
                new AdminRegionalMetricResponseDto.AxisMaxDto(
                        // 축 최대값은 "자릿수 단위 올림"으로 깔끔한 눈금 범위를 만든다.
                        // 예: 45,454 -> 50,000 / 9,876 -> 10,000
                        roundUpByMagnitude(maxSales),
                        roundUpByMagnitude(maxDataUsageGb)
                ),
                new AdminRegionalMetricResponseDto.MaxRegionDto(maxSalesRegion, maxDataUsageRegion)
        );
    }

    /**
     * raw 리스트를 정규화된 지역명 기준 맵으로 변환.
     * - key: normalize(province)
     * - value: 해당 지역 raw row
     * province가 null인 데이터는 무시한다.
     */
    private Map<String, RegionalMetricRawData> toNormalizedMap(List<RegionalMetricRawData> rawData) {
        Map<String, RegionalMetricRawData> map = new LinkedHashMap<>();
        for (RegionalMetricRawData row : rawData) {
            if (row.province() == null) {
                continue;
            }
            map.put(normalize(row.province()), row);
        }
        return map;
    }

    // 공백만 제거하여 표기 차이(띄어쓰기)로 인한 매칭 실패를 방지
    private String normalize(String value) {
        return value.replaceAll("\\s+", "");
    }

    // DB numeric -> 응답 long 변환. null은 0, 소수는 HALF_UP 반올림.
    private long toLong(BigDecimal value) {
        if (value == null) {
            return 0L;
        }
        return value.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /**
     * 값의 가장 높은 자릿수 단위로 올림한다.
     * - 45,454 -> 50,000
     * 차트 축 상한을 사람이 읽기 쉬운 값으로 맞추기 위한 유틸.
     */
    private long roundUpByMagnitude(long value) {
        if (value <= 0) {
            return 0L;
        }
        int digits = String.valueOf(value).length() - 1;
        long unit = (long) Math.pow(10, digits);
        return ((value + unit - 1) / unit) * unit;
    }
}
