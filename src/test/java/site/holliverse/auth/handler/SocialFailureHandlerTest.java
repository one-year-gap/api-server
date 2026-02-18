package site.holliverse.auth.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import static org.assertj.core.api.Assertions.assertThat;

class SocialFailureHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final SocialFailureHandler handler = new SocialFailureHandler(objectMapper);

    @Test
    @DisplayName("OAuth 사용자 정보 누락 에러는 400/OAUTH_USER_INFO_INVALID로 응답한다")
    void returnsBadRequestForInvalidUserInfo() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user_email"),
                "Google OAuth2 user email is missing"
        );

        handler.onAuthenticationFailure(request, response, exception);

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(body.get("errorDetail").get("code").asText()).isEqualTo("OAUTH_USER_INFO_INVALID");
    }
}
