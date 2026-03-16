package site.holliverse.admin.integration.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import site.holliverse.admin.config.SolapiProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
public class SolapiSmsClient {

    private final RestTemplate restTemplate;
    private final SolapiProperties properties;

    public SolapiSmsClient(RestTemplate restTemplate, SolapiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public void sendCouponIssuedMessage(String to, String text) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()
                || properties.apiSecret() == null || properties.apiSecret().isBlank()
                || properties.senderPhone() == null || properties.senderPhone().isBlank()) {
            log.warn("[SolapiSms] 설정이 없어 문자 발송을 건너뜁니다. to={}", to);
            return;
        }

        String baseUrl = properties.baseUrl() != null && !properties.baseUrl().isBlank()
                ? properties.baseUrl()
                : "https://api.solapi.com";
        String url = baseUrl + "/messages/v4/send-many";

        String date = Instant.now().toString();
        String salt = UUID.randomUUID().toString();
        String signature = generateSignature(date, salt, properties.apiSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization",
                "HMAC-SHA256 ApiKey=" + properties.apiKey()
                        + ", Date=" + date
                        + ", salt=" + salt
                        + ", signature=" + signature);

        SolapiSendRequest body = new SolapiSendRequest(
                List.of(new SolapiMessage(properties.senderPhone(), to, text))
        );

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (RestClientException e) {
            log.warn("[SolapiSms] 문자 발송 실패 to={}", to, e);
        }
    }

    private String generateSignature(String date, String salt, String apiSecret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] result = mac.doFinal((date + salt).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(result);
        } catch (Exception e) {
            throw new IllegalStateException("SOLAPI 인증 서명 생성에 실패했습니다.", e);
        }
    }

    public record SolapiSendRequest(
            List<SolapiMessage> messages
    ) {
    }

    public record SolapiMessage(
            String from,
            String to,
            String text
    ) {
    }
}
