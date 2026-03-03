package site.holliverse.admin.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.admin.application.usecase.CounselTrafficUseCase;
import site.holliverse.admin.query.dao.CounselTrafficDailyRawData;
import site.holliverse.admin.query.dao.CounselTrafficMonthlyRawData;
import site.holliverse.auth.jwt.JwtTokenProvider;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("admin")
@WebMvcTest(CounselController.class)
@AutoConfigureMockMvc(addFilters = false)
class CounselControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CounselTrafficUseCase useCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("일 트래픽 조회 성공: 유효한 date 파라미터를 주면 200과 데이터를 반환한다.")
    void get_daily_traffic_success() throws Exception {
        LocalDate date = LocalDate.of(2026, 2, 24);
        given(useCase.getHourlyTraffic(date))
                .willReturn(List.of(new CounselTrafficDailyRawData(0, 3)));

        mockMvc.perform(get("/api/v1/admin/counsel-traffic/daily")
                        .param("date", "2026-02-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.maxCount").value(3))
                .andExpect(jsonPath("$.data.items[0].hour").value(0))
                .andExpect(jsonPath("$.data.items[0].count").value(3));
    }

    @Test
    @DisplayName("일 트래픽 조회 실패: 잘못된 date 형식이면 400을 반환한다.")
    void get_daily_traffic_invalid_date() throws Exception {
        mockMvc.perform(get("/api/v1/admin/counsel-traffic/daily")
                        .param("date", "2026-02-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorDetail.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errorDetail.field").value("date"));

        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("월 트래픽 조회 성공: 유효한 month 파라미터를 주면 200과 데이터를 반환한다.")
    void get_monthly_traffic_success() throws Exception {
        YearMonth month = YearMonth.of(2026, 2);
        given(useCase.getDailyTraffic(month))
                .willReturn(List.of(new CounselTrafficMonthlyRawData(1, 5)));

        mockMvc.perform(get("/api/v1/admin/counsel-traffic/monthly")
                        .param("month", "2026-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.maxCount").value(5))
                .andExpect(jsonPath("$.data.items[0].day").value(1))
                .andExpect(jsonPath("$.data.items[0].count").value(5));
    }

    @Test
    @DisplayName("월 트래픽 조회 실패: 잘못된 month 형식이면 400을 반환한다.")
    void get_monthly_traffic_invalid_month() throws Exception {
        mockMvc.perform(get("/api/v1/admin/counsel-traffic/monthly")
                        .param("month", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorDetail.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errorDetail.field").value("month"));

        verifyNoInteractions(useCase);
    }
}
