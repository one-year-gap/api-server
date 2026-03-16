package site.holliverse.customer.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import site.holliverse.customer.integration.admin.AdminLogFeaturesClient;

/**
 * Admin API log-features 호출용 RestTemplate 및 클라이언트 빈 등록.
 */
@Configuration
@Profile("customer")
@EnableConfigurationProperties(AdminLogFeaturesProperties.class)
public class AdminLogFeaturesConfig {

    @Bean
    public RestTemplate adminLogFeaturesRestTemplate(AdminLogFeaturesProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutMs());
        factory.setReadTimeout(properties.readTimeoutMs());
        return new RestTemplate(factory);
    }

    @Bean
    public AdminLogFeaturesClient adminLogFeaturesClient(
            @org.springframework.beans.factory.annotation.Qualifier("adminLogFeaturesRestTemplate") RestTemplate restTemplate,
            AdminLogFeaturesProperties properties) {
        return new AdminLogFeaturesClient(restTemplate, properties);
    }
}
