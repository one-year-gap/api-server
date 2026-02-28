package site.holliverse.admin.query.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * jOOQ 쿼리 결과를 담는 순수 데이터 객체
 */
public record MemberDetailRawData(
        String encryptedName,     // 암호화된 이름
        String encryptedPhone,    // 암호화된 전화번호
        String email,
        LocalDate birthDate,
        String gender,
        String membership,
        LocalDate joinDate,
        String status,

        // 주소 관련 (address 테이블)
        String province,
        String city,
        String streetAddress,

        // 요금제 (product 테이블 조인 결과)
        String currentMobilePlan,

        LocalDateTime contractStartDate, // 약정 시작일
        Integer contractMonths, // 약정 개월 수
        LocalDateTime contractEndDate, // 약정 만료일

        Long totalSupportCount, // 총 상담 횟수
        LocalDateTime lastSupportDate // 최근 상담 일자
) {
}