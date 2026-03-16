package site.holliverse.admin.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import site.holliverse.admin.integration.sms.SolapiSmsClient;

@Profile("admin")
@Configuration
@EnableConfigurationProperties(SolapiProperties.class)
public class SolapiConfig {

    @Bean
    public RestTemplate solapiRestTemplate(SolapiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutMs());
        factory.setReadTimeout(properties.readTimeoutMs());
        return new RestTemplate(factory);
    }

    @Bean
    public SolapiSmsClient solapiSmsClient(
            @Qualifier("solapiRestTemplate") RestTemplate restTemplate,
            SolapiProperties properties
    ) {
        return new SolapiSmsClient(restTemplate, properties);
    }
}
