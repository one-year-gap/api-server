package site.holliverse.customer.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import site.holliverse.customer.integration.external.AdminLogFeaturesClient;
import site.holliverse.shared.monitoring.CustomerMetrics;
import site.holliverse.shared.monitoring.http.ObservedRestTemplateInterceptor;

/**
 * Admin API log-features 호출용 RestTemplate 및 클라이언트 빈 등록.
 */
@Configuration
@Profile("customer")
@EnableConfigurationProperties(AdminLogFeaturesProperties.class)
public class AdminLogFeaturesConfig {

    @Bean
    public RestTemplate adminLogFeaturesRestTemplate(AdminLogFeaturesProperties properties,
                                                     MeterRegistry meterRegistry) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutMs());
        factory.setReadTimeout(properties.readTimeoutMs());
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(new ObservedRestTemplateInterceptor(meterRegistry, "admin-log-features"));
        return restTemplate;
    }

    @Bean
    public AdminLogFeaturesClient adminLogFeaturesClient(
            @Qualifier("adminLogFeaturesRestTemplate") RestTemplate restTemplate,
            AdminLogFeaturesProperties properties,
            CustomerMetrics customerMetrics) {
        return new AdminLogFeaturesClient(restTemplate, properties, customerMetrics);
    }
}
