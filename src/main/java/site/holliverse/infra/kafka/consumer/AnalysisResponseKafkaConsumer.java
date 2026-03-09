package site.holliverse.infra.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import site.holliverse.admin.application.usecase.HandleAnalysisResponseUseCase;
import site.holliverse.admin.application.usecase.dto.AnalysisResponseCommand;
import site.holliverse.infra.kafka.model.AnalysisResponsePayload;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AnalysisResponseKafkaConsumer {
    private final ObjectMapper mapper;
    private final HandleAnalysisResponseUseCase useCase;

    /**
     * Kafka 메시지를 JSON으로 역직렬화 -> use case를 실행
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.analysis-response}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "analysisResponseKafkaListenerContainerFactory"
    )
    public void consume(
            String payload,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        try {
            AnalysisResponsePayload message = mapper.readValue(payload, AnalysisResponsePayload.class);
            useCase.execute(toCommand(message));
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[Kafka][analysis-response] consume failed. topic={}, offset={}, raw={}", topic, offset, payload, e);
            throw new IllegalStateException("analysis-response consume failed", e);
        }
    }

    private AnalysisResponseCommand toCommand(AnalysisResponsePayload payload) {
        List<AnalysisResponseCommand.KeywordCountCommand> keywordCounts = payload.keywordCounts() == null
                ? null
                : payload.keywordCounts().stream()
                .map(item -> new AnalysisResponseCommand.KeywordCountCommand(
                        item.keywordId(),
                        item.businessKeywordId(),
                        item.keywordCode(),
                        item.keywordName(),
                        item.count()
                ))
                .toList();

        return new AnalysisResponseCommand(
                payload.schema(),
                payload.dispatchRequestId(),
                payload.chunkId(),
                payload.caseId(),
                payload.analyzerVersion(),
                payload.analysisId(),
                payload.memberId(),
                payload.status(),
                payload.keywordTypes(),
                payload.keywordHits(),
                keywordCounts,
                payload.error(),
                payload.producedAt()
        );
    }
}
