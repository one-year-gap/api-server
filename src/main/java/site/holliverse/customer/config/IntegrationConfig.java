package site.holliverse.customer.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import site.holliverse.customer.integration.fastapi.FastApiRecommendationClient;
import site.holliverse.shared.monitoring.CustomerMetrics;

@Configuration
@Profile("customer")
public class IntegrationConfig {

    @Bean
    public FastApiRecommendationClient fastApiRecommendationClient(
            @Qualifier("fastApiRestTemplate") RestTemplate restTemplate,
            FastApiProperties fastApiProperties,
            CustomerMetrics customerMetrics) {
        return new FastApiRecommendationClient(restTemplate, fastApiProperties, customerMetrics);
    }
}
