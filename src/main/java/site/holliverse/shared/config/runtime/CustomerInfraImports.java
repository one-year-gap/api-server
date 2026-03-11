package site.holliverse.shared.config.runtime;

import site.holliverse.customer.config.RecommendationKafkaConfig;
import site.holliverse.infra.kafka.config.KafkaProducerConfiguration;

/**
 * customer 런타임에서만 활성화할 인프라 설정 목록.
 * Enum에 추가된 설정들만 CustomerRuntimeInfraConfiguration을 통해 Import된다.
 */
public enum CustomerInfraImports {
    KAFKA_PRODUCER(KafkaProducerConfiguration.class),
    RECOMMENDATION_KAFKA(RecommendationKafkaConfig.class),
    ;

    private final Class<?> configClass;

    CustomerInfraImports(Class<?> configClass) {
        this.configClass = configClass;
    }

    public Class<?> configClass() {
        return configClass;
    }
}

