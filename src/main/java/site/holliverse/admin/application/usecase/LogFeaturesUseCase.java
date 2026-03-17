package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.domain.model.churn.ChurnEvaluationResult;
import site.holliverse.admin.web.dto.log.LogFeaturesRequestDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * POST /api/v1/admin/log-features 처리.
 */
@Service
@Profile("admin")
@RequiredArgsConstructor
@Slf4j
public class LogFeaturesUseCase {

    private final CalculateLogChurnScoreService calculateLogChurnScoreService;

    /**
     * 로그 처리.
     */
    @Transactional
    public void execute(LogFeaturesRequestDto request) {
        // 대상 이벤트
        List<LogFeatureEvent> events = resolveEvents(request);
        if (events.isEmpty()) {
            log.debug("log-features skipped: member_id={} events=0", request.memberId());
            return;
        }

        // 기준일 추출
        LocalDate baseDate = resolveBaseDate(events);

        // 이탈률 계산
        ChurnEvaluationResult result = calculateLogChurnScoreService.calculateAndStore(
                request.memberId(),
                baseDate,
                events
        );

        log.debug("log-features: member_id={} events={} churnScore={}",
                request.memberId(), events.size(), result.scoreResult().score().value());
    }

    /**
     * 이벤트 변환.
     */
    private List<LogFeatureEvent> resolveEvents(LogFeaturesRequestDto request) {
        Map<Long, LogFeatureEvent> events = new LinkedHashMap<>();
        for (LogFeaturesRequestDto.LogEventDto event : request.events()) {
            LogFeatureEventName.find(event.eventName())
                    .ifPresent(eventName -> events.putIfAbsent(
                            event.eventId(),
                            new LogFeatureEvent(
                                    event.eventId(),
                                    Instant.parse(event.timestamp()),
                                    event.event(),
                                    eventName,
                                    event.eventProperties()
                            )
                    ));
        }

        return List.copyOf(events.values());
    }

    /**
     * 기준일 추출.
     */
    private LocalDate resolveBaseDate(List<LogFeatureEvent> events) {
        return events.stream()
                .map(LogFeatureEvent::timestamp)
                .max(Instant::compareTo)
                .orElseGet(Instant::now)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate();
    }
}
