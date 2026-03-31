package site.holliverse.shared.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import site.holliverse.customer.application.usecase.recommendation.RecommendationPendingFutureRegistry;

@Component
@Profile("customer")
public class CustomerMonitoringBinder {

    public CustomerMonitoringBinder(
            MeterRegistry meterRegistry,
            RecommendationPendingFutureRegistry pendingFutureRegistry,
            @Qualifier("userLogTaskExecutor") ThreadPoolTaskExecutor userLogTaskExecutor,
            @Qualifier("adminLogFeatureTaskExecutor") ThreadPoolTaskExecutor adminLogFeatureTaskExecutor,
            @Qualifier("recommendationTaskExecutor") ThreadPoolTaskExecutor recommendationTaskExecutor
    ) {
        Gauge.builder("holliverse.recommendation.pending.size", pendingFutureRegistry, RecommendationPendingFutureRegistry::size)
                .description("Pending recommendation futures waiting for Kafka completion")
                .register(meterRegistry);

        bindExecutorMetrics(meterRegistry, "user-log", userLogTaskExecutor);
        bindExecutorMetrics(meterRegistry, "admin-log-feature", adminLogFeatureTaskExecutor);
        bindExecutorMetrics(meterRegistry, "recommendation-trigger", recommendationTaskExecutor);
    }

    private void bindExecutorMetrics(
            MeterRegistry meterRegistry,
            String executorName,
            ThreadPoolTaskExecutor executor
    ) {
        Gauge.builder("holliverse.executor.pool.size", executor, ThreadPoolTaskExecutor::getPoolSize)
                .description("Current executor pool size")
                .tag("executor", executorName)
                .register(meterRegistry);

        Gauge.builder("holliverse.executor.active.count", executor, ThreadPoolTaskExecutor::getActiveCount)
                .description("Currently active executor threads")
                .tag("executor", executorName)
                .register(meterRegistry);

        Gauge.builder("holliverse.executor.queue.size", executor,
                        taskExecutor -> taskExecutor.getThreadPoolExecutor().getQueue().size())
                .description("Current executor queue size")
                .tag("executor", executorName)
                .register(meterRegistry);

        Gauge.builder("holliverse.executor.queue.remaining", executor,
                        taskExecutor -> taskExecutor.getThreadPoolExecutor().getQueue().remainingCapacity())
                .description("Remaining executor queue capacity")
                .tag("executor", executorName)
                .register(meterRegistry);
    }
}
