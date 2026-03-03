package site.holliverse.admin.web.dto.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 회원 상세 정보 응답용 DTO
 */
public record AdminMemberDetailResponseDto(
        String name,              // 1. 이름 (복호화 완료된 상태)
        int age,                  // 2. 나이 (생년월일로 계산된 만 나이)
        String membership,        // 3. 등급 (VVIP, VIP 등)
        String gender,            // 4. 성별
        String fullAddress,       // 5. 주소 (province + city + streetAddress)
        String email,             // 6. 이메일
        LocalDate birthDate,      // 7. 생년월일
        String currentMobilePlan, // 8. 현재 가입요금제 (MOBILE_PLAN 기준)
        String phone,             // 9. 연락처 (복호화 완료된 상태)
        LocalDate joinDate,       // 10-1. 가입일
        String joinDurationText,  // 10-2. 가입기간 텍스트 (예: "2년 3개월")
        String status,            // 11. 회원상태 (ACTIVE, BANNED 등)

        // --- 추가된 약정 정보 (무약정일 경우 contract 관련 값들은 null) ---
        boolean isContracted,             // 12. 약정 여부 (true/false)
        Integer contractMonths,           // 13. 약정 개월 수 (12, 24)
        LocalDate contractStartDate,      // 14. 약정 시작일
        LocalDate contractEndDate,        // 15. 약정 만료일
        Integer remainingDays,            // 16. 약정 남은 일수 (D-Day 표기용)
        Boolean isExpired,                // 17. 약정 만료 여부

        // --- 추가된 상담 이력 통계 ---
        long totalSupportCount,           // 18. 총 상담 횟수 (없으면 0)
        LocalDate lastSupportDate         // 19. 최근 상담 일자 (상담 내역 없으면 null)
) {
}