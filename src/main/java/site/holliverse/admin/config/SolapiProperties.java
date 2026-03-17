package site.holliverse.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.solapi")
public record SolapiProperties(
        String baseUrl,
        String apiKey,
        String apiSecret,
        String senderPhone,
        java.util.List<String> allowedTestPhones,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}
