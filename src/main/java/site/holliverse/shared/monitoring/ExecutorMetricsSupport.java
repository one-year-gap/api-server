package site.holliverse.shared.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 주요 비동기 executor 상태를 gauge로 노출.
 */
public final class ExecutorMetricsSupport {

    private ExecutorMetricsSupport() {
    }

    public static void bind(ThreadPoolTaskExecutor executor, String name, MeterRegistry meterRegistry) {
        Gauge.builder("holliverse.executor.active", executor, ThreadPoolTaskExecutor::getActiveCount)
                .description("Active thread count for application executors")
                .tag("name", name)
                .register(meterRegistry);

        Gauge.builder("holliverse.executor.pool.size", executor, ThreadPoolTaskExecutor::getPoolSize)
                .description("Current pool size for application executors")
                .tag("name", name)
                .register(meterRegistry);

        Gauge.builder("holliverse.executor.queue.size", executor,
                        taskExecutor -> taskExecutor.getThreadPoolExecutor().getQueue().size())
                .description("Current queue size for application executors")
                .tag("name", name)
                .register(meterRegistry);

        Gauge.builder("holliverse.executor.queue.remaining", executor,
                        taskExecutor -> taskExecutor.getThreadPoolExecutor().getQueue().remainingCapacity())
                .description("Remaining queue capacity for application executors")
                .tag("name", name)
                .register(meterRegistry);
    }
}
