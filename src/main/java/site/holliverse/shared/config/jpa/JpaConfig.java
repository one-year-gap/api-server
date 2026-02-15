package site.holliverse.shared.config.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // Auditing(자동 시간 주입) 기능 활성화
public class JpaConfig {
}