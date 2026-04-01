package site.holliverse.shared.config.runtime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * customer 런타임에서 활성화할 인프라 설정의 진입점.
 * - KafkaProducerConfiguration
 * - RecommendationKafkaConfig
 * - customer 비동기 Executor
 * Kafka 관련 설정은 CustomerInfraImports Enum을 통해 Import
 */
@Configuration
@Profile("customer")
@EnableKafka
@EnableAsync
@EnableScheduling
@Import(CustomerImportsSelector.class)
public class CustomerRuntimeInfraConfiguration {

    @Bean(name = "userLogTaskExecutor")
    public ThreadPoolTaskExecutor userLogTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(512);
        executor.setThreadNamePrefix("user-log-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(name = "adminLogFeatureTaskExecutor")
    public ThreadPoolTaskExecutor adminLogFeatureTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(512);
        executor.setThreadNamePrefix("admin-log-feature-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(name = "recommendationTaskExecutor")
    public ThreadPoolTaskExecutor recommendationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(128);
        executor.setThreadNamePrefix("recommendation-trigger-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
