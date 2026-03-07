package site.holliverse.customer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FastAPI(LLM 추천) 연동 설정. application.yaml의 external.fastapi와 바인딩.
 */
@ConfigurationProperties(prefix = "external.fastapi")
public record FastApiProperties(
        String baseUrl,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}
