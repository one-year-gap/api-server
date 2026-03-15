package site.holliverse.admin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * admin churn scoring 설정 바인딩.
 */
@Profile("admin")
@Configuration
@EnableConfigurationProperties(ChurnScoringProperties.class)
public class ChurnScoringConfig {
}
