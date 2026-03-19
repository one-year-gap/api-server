package site.holliverse.admin.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import site.holliverse.admin.integration.sms.SolapiSmsClient;
import site.holliverse.shared.monitoring.http.ObservedRestTemplateInterceptor;

@Profile("admin")
@Configuration
@EnableConfigurationProperties(SolapiProperties.class)
public class SolapiConfig {

    @Bean
    public RestTemplate solapiRestTemplate(SolapiProperties properties, MeterRegistry meterRegistry) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutMs());
        factory.setReadTimeout(properties.readTimeoutMs());
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(new ObservedRestTemplateInterceptor(meterRegistry, "solapi"));
        return restTemplate;
    }

    @Bean
    public SolapiSmsClient solapiSmsClient(
            @Qualifier("solapiRestTemplate") RestTemplate restTemplate,
            SolapiProperties properties
    ) {
        return new SolapiSmsClient(restTemplate, properties);
    }
}
