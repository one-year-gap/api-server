package site.holliverse.shared.config;

import java.util.concurrent.Executor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import site.holliverse.shared.monitoring.ExecutorMetricsSupport;

/**
 * 비동기 실행 설정.
 * UserLog publish, 추천 FastAPI 트리거 등 요청별로 전용 Executor를 둠.
 */
@Configuration
@EnableAsync
@Profile("customer")
public class AsyncConfig {

    @Bean(name = "userLogTaskExecutor")
    public Executor userLogTaskExecutor(MeterRegistry meterRegistry) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(512);
        executor.setThreadNamePrefix("user-log-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        ExecutorMetricsSupport.bind(executor, "user-log", meterRegistry);
        return executor;
    }

    /** 추천 캐시 미스 시 FastAPI 202 트리거 전용. 로그 태스크와 분리해 부하·튜닝을 나눔. */
    @Bean(name = "recommendationTaskExecutor")
    public Executor recommendationTaskExecutor(MeterRegistry meterRegistry) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(128);
        executor.setThreadNamePrefix("recommendation-trigger-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        ExecutorMetricsSupport.bind(executor, "recommendation-trigger", meterRegistry);
        return executor;
    }
}
