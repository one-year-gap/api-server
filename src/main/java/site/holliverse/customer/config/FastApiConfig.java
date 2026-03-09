package site.holliverse.customer.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * FastAPI(LLM 추천) 연동용 RestTemplate.
 * connect/read 타임아웃만 설정하고, base-url은 클라이언트에서 사용.
 */
@Configuration
@EnableConfigurationProperties(FastApiProperties.class)
public class FastApiConfig {

    @Bean
    public RestTemplate fastApiRestTemplate(FastApiProperties fastApiProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(fastApiProperties.connectTimeoutMs());
        factory.setReadTimeout(fastApiProperties.readTimeoutMs());
        return new RestTemplate(factory);
    }
}
