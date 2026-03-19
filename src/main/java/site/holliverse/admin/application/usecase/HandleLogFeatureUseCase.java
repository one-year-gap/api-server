package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.web.dto.log.LogFeatureWebhookRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Profile("admin")
public class HandleLogFeatureUseCase {

    private final CalculateLogChurnScoreService calculateLogChurnScoreService;

    /**
     * 실시간 로그 처리.
     *
     * 1. feature_snapshot_store memberId 조회
     * 2. member_action_feature 없으면 생성
     * 3. member_action_feature 요금제 비교 이력/위약금 확인 이력 UPDATE
     * 4. feature_snapshot_store:
     *                            feature_type = 'MEMBER_ACTION_FEATURE'
     *                            feature_score 계산(계산 오케스트레이션 계층 사용)
     */
    @Transactional
    public void execute(LogFeatureWebhookRequest request) {
        UserActionFeatureEventName eventName = resolveEventName(request)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 로그 이벤트입니다. eventType=" + request.eventType()));

        calculateLogChurnScoreService.calculateAndStore(
                request.memberId(),
                resolveBaseDate(request.timeStamp()),
                List.of(new LogFeatureEvent(
                        resolveEventId(request),
                        Instant.parse(request.timeStamp()),
                        "click",
                        eventName,
                        Map.of()
                ))
        );
    }

    /**
     * 위약금 확인 이력 개수 증가 메서드
     */
    private Optional<UserActionFeatureEventName> resolveEventName(LogFeatureWebhookRequest request) {
        return UserActionFeatureEventName.find(request.eventType().value());
    }


    /**
     * 기준일 추출.
     */
    private LocalDate resolveBaseDate(String timestamp) {
        return Instant.parse(timestamp)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate();
    }

    /**
     * 이벤트 식별자 생성.
     */
    private long resolveEventId(LogFeatureWebhookRequest request) {
        return Integer.toUnsignedLong((request.memberId() + "|" + request.eventType().value() + "|" + request.timeStamp()).hashCode());
    }

}
