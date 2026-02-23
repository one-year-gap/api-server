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

    // 지역 코드 기준표.
    // key   : 응답에 내려줄 표준 지역명
    // value : 고정 지역 코드
    // 변경되지 않는 기준 데이터이므로 static final + 불변 컬렉션으로 관리한다.
    private static final Map<String, String> REGION_CODE_MAP;

    // 응답 지역 순서를 고정하기 위한 목록.
    // DB에 데이터가 없어도 이 순서대로 모든 지역을 내려준다.
    private static final List<String> REGIONS;

    /**
     * 컴포넌트가 싱글톤이어도 기준표 자체는 "인스턴스 상태"가 아니라
     * "클래스 기준 상수"이므로 static으로 분리한다.
     *
     * 또한 Map.copyOf / List.copyOf로 감싸서
     * put/add 같은 내부 변경과 참조 재할당을 모두 막는다.
     */
    static {
        // LinkedHashMap을 사용해 삽입 순서(=응답 순서)를 보장한다.
        Map<String, String> map = new LinkedHashMap<>();
        map.put("서울특별시", "R001");
        map.put("인천광역시", "R002");
        map.put("경기도", "R003");
        map.put("강원도", "R004");
        map.put("충청남도", "R005");
        map.put("세종특별자치시", "R006");
        map.put("대전광역시", "R007");
        map.put("충청북도", "R008");
        map.put("경상북도", "R009");
        map.put("대구광역시", "R010");
        map.put("울산광역시", "R011");
        map.put("부산광역시", "R012");
        map.put("경상남도", "R013");
        map.put("전라북도", "R014");
        map.put("광주광역시", "R015");
        map.put("전라남도", "R016");
        map.put("제주특별자치도", "R017");

        REGION_CODE_MAP = Map.copyOf(map);
        // map의 키 순서를 그대로 복사해 결정적(deterministic) 응답 순서를 유지한다.
        REGIONS = List.copyOf(map.keySet());
    }

    /**
     * 지역 통계 응답을 구성한다.
     *
     * 처리 규칙:
     * 1) 사전 정의된 모든 지역을 항상 응답에 포함한다.
     * 2) 특정 지역 데이터가 없으면 평균값을 0으로 채운다.
     * 3) regionCode는 REGION_CODE_MAP 기준으로 고정 반환한다.
     * 4) axisMax는 차트 축 계산을 위해 자리수 기준 올림한다.
     */
    public AdminRegionalMetricResponseDto toResponse(List<RegionalMetricRawData> rawData) {
        // DB 결과를 "정규화된 지역명 -> raw row" 맵으로 변환해 조회 비용을 줄인다.
        Map<String, RegionalMetricRawData> normalizedMap = toNormalizedMap(rawData);

        long maxSales = 0L;
        long maxDataUsageGb = 0L;

        //일단 최대 지역을 서울로 초기화
        String maxSalesRegion = REGIONS.get(0);
        String maxDataUsageRegion = REGIONS.get(0);

        List<AdminRegionalMetricResponseDto.RegionMetricDto> regions = REGIONS.stream()
                .map(region -> {
                    RegionalMetricRawData data = normalizedMap.get(normalize(region));

                    // 고정지역 목록을 순회하면서 고정지역 목록이 null 값이면 매출 0 처리
                    // 사용량도 0 처리
                    //값이 있다면 그냥 값 주입
                    long avgSales = toLong(data == null ? null : data.avgSales());
                    long avgDataUsage = toLong(data == null ? null : data.avgDataUsageGb());

                    //지역을 dto 로 만든다.
                    return new AdminRegionalMetricResponseDto.RegionMetricDto(
                            toRegionCode(region),
                            region,
                            avgSales,
                            avgDataUsage
                    );
                })
                .toList();


        // 평균 최대 매출, 평균 최대 사용량 선정한다.
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
                        roundUpByMagnitude(maxSales),
                        roundUpByMagnitude(maxDataUsageGb)
                ),
                new AdminRegionalMetricResponseDto.MaxRegionDto(maxSalesRegion, maxDataUsageRegion)
        );
    }

    /**
     * raw DB 결과를 정규화 키 맵으로 변환한다.
     * 예: "부산 광역시" / "부산광역시" 같은 표기 차이를 공백 제거로 흡수한다.
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

    private String normalize(String value) {
        return value.replaceAll("\\s+", "");
    }

    // BigDecimal -> long 변환. null은 0, 소수점은 HALF_UP 반올림.
    private long toLong(BigDecimal value) {
        if (value == null) {
            return 0L;
        }
        return value.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    // 차트 축 최대값을 자리수 기준 올림한다.
    // 예: 45454 -> 50000, 9876 -> 10000
    private long roundUpByMagnitude(long value) {
        if (value <= 0) {
            return 0L;
        }
        int digits = String.valueOf(value).length() - 1;
        long unit = (long) Math.pow(10, digits);
        return ((value + unit - 1) / unit) * unit;
    }

    // 고정 지역코드를 반환한다. 예외 지역명은 방어적으로 R000 처리.
    private String toRegionCode(String region) {
        return REGION_CODE_MAP.getOrDefault(region, "R000");
    }
}