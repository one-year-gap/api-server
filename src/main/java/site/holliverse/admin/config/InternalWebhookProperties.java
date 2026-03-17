package site.holliverse.admin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * admin 내부 webhook 보안 설정.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.internal-webhook")
public class InternalWebhookProperties {

    private String pathPattern = "/internal/**";
}
