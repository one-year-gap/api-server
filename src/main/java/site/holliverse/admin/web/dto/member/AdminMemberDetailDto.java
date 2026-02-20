package site.holliverse.admin.web.dto.member;

import java.time.LocalDate;

/**
 * 회원 상세 정보 응답용 DTO
 */
public record AdminMemberDetailDto(
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
        long joinDurationDays,    // 10-2. 가입기간 (가입일로부터 D+며칠인지)
        String status             // 11. 회원상태 (ACTIVE, BANNED 등)
) {
}