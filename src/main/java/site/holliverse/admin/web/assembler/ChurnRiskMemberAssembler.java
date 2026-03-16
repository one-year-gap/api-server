package site.holliverse.admin.web.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.query.dao.ChurnRiskMemberRawData;
import site.holliverse.admin.web.dto.churn.ChurnRiskMemberDto;
import site.holliverse.admin.web.dto.churn.ChurnRiskMemberListResponseDto;
import site.holliverse.shared.util.DecryptionTool;

import java.util.Collections;
import java.util.List;

/**
 * DAO RawData를 실제 화면 응답 DTO로 변환하는 조립기.
 *
 * 여기서 수행하는 작업:
 * - 이름 복호화 및 가운데 마스킹
 * - 전화번호 복호화 및 010-****-1234 형식 마스킹
 * - risk_reasons JSON 배열 중 첫 번째 값만 추출
 * - 페이지 기준이 아닌 "전체 기준 연속 번호" 계산
 *
 * 즉, 화면에 맞는 최종 표현 형태를 만드는 계층이다.
 */
@Profile("admin")
@Component
@RequiredArgsConstructor
public class ChurnRiskMemberAssembler {

    /** 이름 / 전화번호 복호화용 */
    private final DecryptionTool decryptionTool;
    /** risk_reasons JSON 배열 파싱용 */
    private final ObjectMapper objectMapper;

    /**
     * 목록 전체를 화면 응답 DTO로 변환한다.
     *
     * startNo는 페이지별 시작 번호다.
     * 예:
     * - 1페이지 size=10 -> 1부터 시작
     * - 2페이지 size=10 -> 11부터 시작
     */
    public ChurnRiskMemberListResponseDto toListResponse(List<ChurnRiskMemberRawData> rawDataList, int totalCount, int page, int size) {
        int startNo = (page - 1) * size + 1;

        List<ChurnRiskMemberDto> members = java.util.stream.IntStream.range(0, rawDataList.size())
                .mapToObj(index -> toDto(rawDataList.get(index), startNo + index))
                .toList();

        return ChurnRiskMemberListResponseDto.of(members, totalCount, page, size);
    }

    /**
     * RawData 한 건을 실제 화면 행(row) DTO로 변환한다.
     */
    private ChurnRiskMemberDto toDto(ChurnRiskMemberRawData raw, int no) {
        String decryptedName = decryptionTool.decrypt(raw.getEncryptedName());
        String decryptedPhone = decryptionTool.decrypt(raw.getEncryptedPhone());

        return ChurnRiskMemberDto.builder()
                .no(no)
                .memberId(raw.getMemberId())
                .membership(raw.getMembership())
                .name(maskName(decryptedName))
                .riskLevel(raw.getRiskLevel())
                .riskReason(extractFirstRiskReason(raw.getRiskReasons()))
                .churnScore(raw.getChurnScore())
                .phone(maskPhone(decryptedPhone))
                .email(raw.getEmail())
                .build();
    }

    /**
     * risk_reasons JSON 배열에서 첫 번째 사유만 추출한다.
     *
     * 예:
     *
     *
     * JSON ["장기 미접속", "상담 만족도 저하"] -> "장기 미접속"파싱 실패 시 전체 API를 깨뜨리지 않도록 null을 반환한다.
     */
    private String extractFirstRiskReason(String riskReasons) {
        if (riskReasons == null || riskReasons.isBlank()) {
            return null;
        }

        try {
            List<String> reasons = objectMapper.readValue(riskReasons, new TypeReference<>() {});
            if (reasons == null || reasons.isEmpty()) {
                return null;
            }
            return reasons.get(0);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 이름 가운데 마스킹.
     * 예:
     * - 김철수 -> 김*수
     * - 홍길동 -> 홍*동
     * - 김수 -> 김*
     */
    private String maskName(String name) {
        if (name == null || name.length() < 2) return name;
        if (name.length() == 2) return name.charAt(0) + "*";

        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    /**
     * 전화번호 마스킹.
     * 예:
     * - 01012345678 -> 010-****-5678
     *
     * 이미 하이픈이 포함되어 들어와도 먼저 제거한 뒤 동일 포맷으로 다시 만든다.
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) return phone;

        String cleanPhone = phone.replaceAll("-", "");
        return cleanPhone.substring(0, 3) + "-****-" + cleanPhone.substring(cleanPhone.length() - 4);
    }
}
