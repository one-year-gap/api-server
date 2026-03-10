package site.holliverse.shared.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 실행 설정.
 * UserLog publish 등 즉시 202 응답이 필요한 요청에서 Kafka 전송을 별도 스레드로 위임.
 */
@Configuration
@EnableAsync
@Profile("customer")
public class AsyncConfig {

    @Bean(name = "userLogTaskExecutor")
    public Executor userLogTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(512);
        executor.setThreadNamePrefix("user-log-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
