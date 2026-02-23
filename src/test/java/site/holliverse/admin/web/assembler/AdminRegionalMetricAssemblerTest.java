package site.holliverse.admin.web.assembler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.holliverse.admin.query.dao.RegionalMetricRawData;
import site.holliverse.admin.web.dto.analytics.AdminRegionalMetricResponseDto;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminRegionalMetricAssemblerTest {

    private final AdminRegionalMetricAssembler assembler = new AdminRegionalMetricAssembler();

    @Test
    @DisplayName("지역별 응답 생성 시 축 최대값을 자리수 올림 처리한다.")
    void toResponse_roundUpAxisMax_success() {
        List<RegionalMetricRawData> rawData = List.of(
                new RegionalMetricRawData("서울특별시", BigDecimal.valueOf(45454), BigDecimal.valueOf(9876)),
                new RegionalMetricRawData("부산광역시", BigDecimal.valueOf(32000), BigDecimal.valueOf(12345))
        );

        AdminRegionalMetricResponseDto result = assembler.toResponse(rawData);

        assertThat(result.regions()).hasSize(17);
        assertThat(result.axisMax().salesAxisMax()).isEqualTo(50000L);
        assertThat(result.axisMax().dataUsageAxisMaxGb()).isEqualTo(20000L);
        assertThat(result.maxRegion().salesRegion()).isEqualTo("서울특별시");
        assertThat(result.maxRegion().dataUsageRegion()).isEqualTo("부산광역시");
    }
}