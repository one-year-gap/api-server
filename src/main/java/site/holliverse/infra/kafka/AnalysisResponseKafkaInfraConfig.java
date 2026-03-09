package site.holliverse.infra.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import site.holliverse.admin.application.usecase.HandleAnalysisResponseUseCase;
import site.holliverse.infra.kafka.consumer.AnalysisResponseKafkaConsumer;

public class AnalysisResponseKafkaInfraConfig {
    /**
     * 분석 응답 컨슈머 Bean 수동 등록
     */
    @Bean
    public AnalysisResponseKafkaConsumer analysisResponseKafkaConsumer(
            ObjectMapper mapper,
            HandleAnalysisResponseUseCase useCase
    ) {
        return new AnalysisResponseKafkaConsumer(mapper, useCase);
    }

    /**
     * 분석 응답 토픽 전용 Listener Container Factory를 구성
     */
    @Bean(name = "analysisResponseKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> analysisKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(3);
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
