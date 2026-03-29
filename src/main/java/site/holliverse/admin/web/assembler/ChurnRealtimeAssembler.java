package site.holliverse.admin.web.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.application.usecase.RetrieveChurnRealtimeUseCase.RetrieveChurnRealtimeResult;
import site.holliverse.admin.query.dao.ChurnRealtimeRawData;
import site.holliverse.admin.web.dto.churn.ChurnRealTimeResponseDto;
import site.holliverse.shared.util.DecryptionTool;

import java.util.List;

@Profile("admin")
@Component
@Slf4j
@RequiredArgsConstructor
public class ChurnRealtimeAssembler {
    private final DecryptionTool decryptionTool;
    private final ObjectMapper objectMapper;

    /**
     * 화면 응답 포맷으로 변환-> 빈 결과면 기존 afterId를 유지
     */
    public ChurnRealTimeResponseDto toResponse(RetrieveChurnRealtimeResult result, Long fallbackAfterId) {
        List<ChurnRealTimeResponseDto.Item> items = result.items().stream()
                .map(this::toItem)
                .toList();

        long afterId = items.isEmpty() ? (fallbackAfterId == null ? 0L : fallbackAfterId) : result.afterId();

        return ChurnRealTimeResponseDto.builder()
                .items(items)
                .afterId(afterId)
                .hasMore(result.hasMore())
                .build();
    }

    /**
     * 이름은 복호화 후 마스킹 -> 사유는 첫 번째 summary만 노출
     */
    private ChurnRealTimeResponseDto.Item toItem(ChurnRealtimeRawData rawData) {
        return ChurnRealTimeResponseDto.Item.builder()
                .churnId(rawData.getChurnId())
                .memberId(rawData.getMemberId())
                .reason(extractFirstRiskReason(rawData.getRiskReasons()))
                .churnLevel(rawData.getChurnLevel())
                .memberName(maskName(decryptionTool.decrypt(rawData.getEncryptedName())))
                .timeStamp(rawData.getTimeStamp())
                .build();
    }

    /**
     * risk_reasons 배열 중 첫 번째 summary를 목록용 문구로 사용
     */
    private String extractFirstRiskReason(String riskReasons) {
        if (riskReasons == null || riskReasons.isBlank()) {
            return null;
        }

        try {
            List<?> reasons = objectMapper.readValue(riskReasons, new TypeReference<>() {});
            if (reasons == null || reasons.isEmpty()) {
                return null;
            }

            Object first = reasons.get(0);
            if (first instanceof String reason) {
                return reason;
            }
            if (first instanceof java.util.Map<?, ?> reasonMap) {
                Object summary = reasonMap.get("summary");
                return summary != null ? summary.toString() : null;
            }
            return null;
        } catch (Exception e) {
            log.warn("risk_reasons JSON 파싱 실패. raw value: {}", riskReasons, e);
            return null;
        }
    }

    /**
     * 대시보드 카드용 이름 마스킹.
     */
    private String maskName(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*님";
        }

        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1) + "님";
    }
}
