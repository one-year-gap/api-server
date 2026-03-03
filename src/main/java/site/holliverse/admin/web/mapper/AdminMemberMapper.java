package site.holliverse.admin.web.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.query.dao.MemberDetailRawData;
import site.holliverse.admin.web.dto.member.AdminMemberDetailResponseDto;
import site.holliverse.shared.util.DecryptionTool;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

@Profile("admin")
@Component
@RequiredArgsConstructor
public class AdminMemberMapper {

    private final DecryptionTool decryptionTool;

    public AdminMemberDetailResponseDto toResponse(MemberDetailRawData rawData) {
        LocalDate today = LocalDate.now();

        // 1. 나이 계산 (만 나이)
        int age = 0;
        if (rawData.birthDate() != null) {
            age = Period.between(rawData.birthDate(), today).getYears();
        }

        // 2. 가입 기간 계산
        String joinDurationText = "";

        if (rawData.joinDate() != null) {
            // 연/월/일 단위 분리
            Period period = Period.between(rawData.joinDate(), today);
            int years = period.getYears();
            int months = period.getMonths();
            int days = period.getDays();

            // 텍스트 포맷팅 ("2년 3개월", "6개월", "15일" 등)
            if (years > 0) {
                joinDurationText = months > 0 ? String.format("%d년 %d개월", years, months) : String.format("%d년", years);
            } else if (months > 0) {
                joinDurationText = String.format("%d개월", months);
            } else {
                joinDurationText = String.format("%d일", days);
            }
        }

        // 3. 주소 문자열 조합
        String fullAddress = "";
        if (rawData.province() != null && rawData.city() != null && rawData.streetAddress() != null) {
            fullAddress = String.format("%s %s %s", rawData.province(), rawData.city(), rawData.streetAddress());
        }

        // 4. 이름과 전화번호 복호화
        String decryptedName = rawData.encryptedName() != null ? decryptionTool.decrypt(rawData.encryptedName()) : null;
        String decryptedPhone = rawData.encryptedPhone() != null ? decryptionTool.decrypt(rawData.encryptedPhone()) : null;

        // 5. 약정 정보 및 D-Day 계산 로직
        boolean isContracted = false; // 약정 여부 (기본값 false)
        Integer remainingDays = null;
        Boolean isExpired = null;

        // 프론트에 보낼 날짜 변수들
        LocalDate finalContractStartDate = null;
        LocalDate finalContractEndDate = null;

        // 약정 개월 수가 존재하고 0보다 크면 "약정 중"으로 판단
        if (rawData.contractMonths() != null && rawData.contractMonths() > 0) {
            isContracted = true;

            // 약정 중일 때만 시작일과 만료일 세팅
            if (rawData.contractStartDate() != null) {
                finalContractStartDate = rawData.contractStartDate().toLocalDate();
            }

            if (rawData.contractEndDate() != null) {
                finalContractEndDate = rawData.contractEndDate().toLocalDate();
                // 남은 일수 계산 (만료일 - 오늘)
                remainingDays = (int) ChronoUnit.DAYS.between(today, rawData.contractEndDate().toLocalDate());
                // 남은 일수가 0보다 작으면 만료된 것
                isExpired = remainingDays < 0;
            }
        }

        long supportCount = rawData.totalSupportCount() != null ? rawData.totalSupportCount() : 0L;
        LocalDate finalLastSupportDate = rawData.lastSupportDate() != null ? rawData.lastSupportDate().toLocalDate() : null;

        // 7. 최종 응답 DTO(record) 생성 후 반환
        return new AdminMemberDetailResponseDto(
                decryptedName,
                age,
                rawData.membership(),
                rawData.gender(),
                fullAddress,
                rawData.email(),
                rawData.birthDate(),
                rawData.currentMobilePlan(),
                decryptedPhone,
                rawData.joinDate(),
                joinDurationText,
                rawData.status(),

                isContracted,                // 약정 여부
                rawData.contractMonths(),    // 약정 개월수 (12 or 24)
                finalContractStartDate,      // 약정 시작일
                finalContractEndDate,        // 약정 만료일
                remainingDays,               // 남은 일수
                isExpired,                   // 만료 여부

                supportCount,
                finalLastSupportDate
        );
    }
}