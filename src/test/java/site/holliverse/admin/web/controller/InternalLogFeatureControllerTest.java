package site.holliverse.admin.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.admin.application.usecase.HandleLogFeatureUseCase;
import site.holliverse.admin.application.usecase.LogFeaturesUseCase;
import site.holliverse.auth.jwt.JwtTokenProvider;
import site.holliverse.shared.error.ApiErrorResponseFactory;
import site.holliverse.shared.error.ConstraintExceptionMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("admin")
@WebMvcTest(InternalLogFeatureController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiErrorResponseFactory.class, ConstraintExceptionMapper.class})
class InternalLogFeatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HandleLogFeatureUseCase handleLogFeatureUseCase;

    @MockitoBean
    private LogFeaturesUseCase logFeaturesUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("단건 내부 로그 feature 요청은 202를 반환한다.")
    void receive_singleRequest_returnsAccepted() throws Exception {
        String body = """
                {
                  "eventType": "click_compare",
                  "memberId": 9243,
                  "timeStamp": "2026-04-01T04:36:06.906491988Z"
                }
                """;

        mockMvc.perform(post("/internal/v1/log-features")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isAccepted());

        verify(handleLogFeatureUseCase).execute(any());
    }

    @Test
    @DisplayName("배치 내부 로그 feature 요청은 202를 반환한다.")
    void receive_batchRequest_returnsAccepted() throws Exception {
        String body = """
                {
                  "memberId": 9243,
                  "events": [
                    {
                      "eventId": 1,
                      "timestamp": "2026-04-01T04:36:06.906491988Z",
                      "event": "click",
                      "eventName": "click_compare",
                      "eventProperties": {}
                    },
                    {
                      "eventId": 2,
                      "timestamp": "2026-04-01T04:36:08.906491988Z",
                      "event": "click",
                      "eventName": "click_penalty",
                      "eventProperties": {}
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/internal/v1/log-features/batch")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isAccepted());

        verify(logFeaturesUseCase).execute(any());
    }
}
