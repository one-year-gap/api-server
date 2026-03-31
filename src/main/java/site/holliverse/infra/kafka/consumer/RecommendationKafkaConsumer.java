package site.holliverse.infra.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import site.holliverse.customer.application.usecase.recommendation.RecommendationKafkaConsumeUseCase;
import site.holliverse.customer.integration.kafka.dto.RecommendationMessagePayload;
import site.holliverse.shared.monitoring.CustomerMetrics;

/**
 * recommendation-topic 메시지 수신 → persona_recommendation upsert → 대기 중인 CompletableFuture 완료.
 */
@Slf4j
@Profile("customer")
@RequiredArgsConstructor
public class RecommendationKafkaConsumer {
    private static final String TRACE_ID_FALLBACK = "NA";

    private final ObjectMapper objectMapper;
    private final RecommendationKafkaConsumeUseCase recommendationKafkaConsumeUseCase;
    private final CustomerMetrics customerMetrics;

    @KafkaListener(
            topics = "${spring.kafka.topic.recommendation}",
            groupId = "${app.kafka.recommendation-consumer-group-id}",
            containerFactory = "recommendationKafkaListenerContainerFactory"
    )
    public void consume(
            String payload,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        long consumeStartedAt = System.nanoTime();
        Long memberIdForCleanup = null;
        String traceId = TRACE_ID_FALLBACK;
        var timerSample = customerMetrics.startSample();
        String outcome = "error";
        try {
            log.info(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} topic={} offset={}",
                    traceId, "unknown", "received", elapsedMs(consumeStartedAt), topic, offset
            );

            RecommendationMessagePayload message = objectMapper.readValue(
                    payload, RecommendationMessagePayload.class);

            memberIdForCleanup = message.memberId();
            traceId = message.traceId() != null && !message.traceId().isBlank()
                    ? message.traceId()
                    : TRACE_ID_FALLBACK;
            log.info(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} topic={} offset={}",
                    traceId, message.memberId(), "parsed", elapsedMs(consumeStartedAt), topic, offset
            );

            log.info(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} segment={} product_count={}",
                    traceId, message.memberId(), "upsert_start", elapsedMs(consumeStartedAt),
                    message.segment(),
                    message.recommendedProducts() != null ? message.recommendedProducts().size() : 0
            );

            RecommendationKafkaConsumeUseCase.RecommendationKafkaConsumeResult result =
                    recommendationKafkaConsumeUseCase.execute(message);
            log.info(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} updated_at={} product_count={}",
                    traceId, result.memberId(), "upsert_done", elapsedMs(consumeStartedAt),
                    result.updatedAt(), result.productCount()
            );

            outcome = result.outcome();
            if ("completed_pending".equals(result.outcome())) {
                log.info(
                        "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} outcome={}",
                        traceId, result.memberId(), "future_completed", elapsedMs(consumeStartedAt), outcome
                );
            } else {
                log.info(
                        "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} outcome={}",
                        traceId, result.memberId(), "no_pending_future", elapsedMs(consumeStartedAt), outcome
                );
            }

            ack.acknowledge();
            log.info(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} topic={} offset={}",
                    traceId, result.memberId(), "ack_done", elapsedMs(consumeStartedAt), topic, offset
            );
        } catch (Exception e) {
            recommendationKafkaConsumeUseCase.completeExceptionally(memberIdForCleanup, e);
            log.error(
                    "[REC][trace_id={}][member_id={}] stage={} elapsed_ms={} topic={} offset={} error={} raw={}",
                    traceId,
                    memberIdForCleanup != null ? memberIdForCleanup : "unknown",
                    "consume_failed",
                    elapsedMs(consumeStartedAt),
                    topic,
                    offset,
                    e.getMessage(),
                    payload,
                    e
            );
            throw new IllegalStateException("recommendation consume failed", e);
        } finally {
            customerMetrics.stopRecommendationKafkaConsume(timerSample, outcome);
        }
    }

    private long elapsedMs(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000;
    }
}
