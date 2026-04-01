package site.holliverse.shared.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("customer")
public class CustomerMetrics {

    private final MeterRegistry meterRegistry;

    public CustomerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startSample() {
        return Timer.start(meterRegistry);
    }

    public void recordRecommendationRequest(String outcome) {
        Counter.builder("holliverse.recommendation.requests")
                .description("Recommendation request outcomes")
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();
    }

    public void stopRecommendationDuration(Timer.Sample sample, String outcome, String source) {
        sample.stop(Timer.builder("holliverse.recommendation.duration")
                .description("End-to-end recommendation request duration")
                .tag("outcome", outcome)
                .tag("source", source)
                .register(meterRegistry));
    }

    public void stopRecommendationWaitDuration(Timer.Sample sample, String outcome) {
        sample.stop(Timer.builder("holliverse.recommendation.wait.duration")
                .description("Recommendation wait time for asynchronous result")
                .tag("outcome", outcome)
                .register(meterRegistry));
    }

    public void recordRecommendationTrigger(String status) {
        Counter.builder("holliverse.recommendation.fastapi.trigger")
                .description("FastAPI trigger results for recommendations")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void stopRecommendationKafkaConsume(Timer.Sample sample, String outcome) {
        sample.stop(Timer.builder("holliverse.recommendation.kafka.consume.duration")
                .description("Recommendation Kafka consumer processing duration")
                .tag("outcome", outcome)
                .register(meterRegistry));
    }

    public void recordUserLogBatchSize(int size) {
        DistributionSummary.builder("holliverse.userlog.batch.size")
                .description("User-log batch payload size")
                .baseUnit("requests")
                .register(meterRegistry)
                .record(size);
    }

    public void recordUserLogPublish(String eventName, String result) {
        Counter.builder("holliverse.userlog.publish")
                .description("User-log publish results")
                .tag("event_name", eventName)
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void stopAdminLogFeatureDuration(Timer.Sample sample, String result) {
        stopAdminLogFeatureDuration(sample, result, "single");
    }

    public void stopAdminLogFeatureDuration(Timer.Sample sample, String result, String mode) {
        sample.stop(Timer.builder("holliverse.userlog.admin_log_feature.duration")
                .description("Admin log-feature API call duration")
                .tag("result", result)
                .tag("mode", mode)
                .register(meterRegistry));
    }

    public void recordAdminLogFeatureDispatch(String result) {
        recordAdminLogFeatureDispatch(result, "single");
    }

    public void recordAdminLogFeatureDispatch(String result, String mode) {
        Counter.builder("holliverse.userlog.admin_log_feature.dispatch")
                .description("Admin log-feature async dispatch enqueue results")
                .tag("result", result)
                .tag("mode", mode)
                .register(meterRegistry)
                .increment();
    }

    public void recordAdminLogFeatureOutbox(String result) {
        Counter.builder("holliverse.userlog.admin_log_feature.outbox")
                .description("Admin log-feature outbox lifecycle results")
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void recordAdminLogFeatureBatchSize(int size) {
        DistributionSummary.builder("holliverse.userlog.admin_log_feature.batch.size")
                .description("Admin log-feature batch request size")
                .baseUnit("events")
                .register(meterRegistry)
                .record(size);
    }
}
