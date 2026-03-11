package site.holliverse.infra.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import site.holliverse.customer.application.usecase.recommendation.RecommendationPendingFutureRegistry;
import site.holliverse.customer.persistence.repository.PersonaRecommendationRepository;
import site.holliverse.infra.kafka.consumer.RecommendationKafkaConsumer;

/**
 * Customer 프로필에서 recommendation-topic 구독을 위한 Kafka 설정.
 * 단일 인스턴스 전제로 동작 (다중 인스턴스 시 대기 중인 Future가 다른 서버에 있어 응답 불가).
 */
@Configuration
public class RecommendationKafkaConfig {

    @Bean(name = "recommendationKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> recommendationKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(2);
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public RecommendationKafkaConsumer recommendationKafkaConsumer(
            ObjectMapper objectMapper,
            PersonaRecommendationRepository personaRecommendationRepository,
            RecommendationPendingFutureRegistry pendingFutureRegistry
    ) {
        return new RecommendationKafkaConsumer(objectMapper, personaRecommendationRepository, pendingFutureRegistry);
    }
}

