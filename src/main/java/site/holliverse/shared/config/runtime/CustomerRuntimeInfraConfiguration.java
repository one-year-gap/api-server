package site.holliverse.shared.config.runtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * customer 런타임에서 활성화할 인프라 설정의 진입점.
 * - KafkaProducerConfiguration
 * - RecommendationKafkaConfig
 * 등은 CustomerInfraImports Enum을 통해서만 Import된다.
 */
@Configuration
@Profile("customer")
@EnableKafka
@Import(CustomerImportsSelector.class)
public class CustomerRuntimeInfraConfiguration {
}

