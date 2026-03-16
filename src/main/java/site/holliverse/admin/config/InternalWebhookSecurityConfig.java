package site.holliverse.admin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * admin 내부 webhook 전용 보안 체인.
 */
@Configuration
@Profile("admin")
@EnableConfigurationProperties(InternalWebhookProperties.class)
public class InternalWebhookSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain internalWebhookChain(
            HttpSecurity http,
            InternalWebhookProperties internalWebhookProperties
    ) throws Exception {
        return http
                .securityMatcher(internalWebhookProperties.getPathPattern())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll()
                )
                .build();
    }
}
