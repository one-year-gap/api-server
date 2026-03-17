package site.holliverse.customer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Admin 내부 log-features 호출 설정. application*.yml의 app.admin과 바인딩.
 * base-url이 비어 있으면 HTTP 호출을 하지 않음(no-op).
 */
@ConfigurationProperties(prefix = "app.admin")
public record AdminLogFeaturesProperties(
        String baseUrl,
        String logFeaturesPath,
        int connectTimeoutMs,
        int readTimeoutMs
) {
    public AdminLogFeaturesProperties {
        if (connectTimeoutMs <= 0) connectTimeoutMs = 3_000;
        if (readTimeoutMs <= 0) readTimeoutMs = 5_000;
        if (logFeaturesPath == null || logFeaturesPath.isBlank()) {
            logFeaturesPath = "/internal/v1/log-features";
        }
    }

    public boolean isEnabled() {
        return baseUrl != null && !baseUrl.isBlank();
    }
}
