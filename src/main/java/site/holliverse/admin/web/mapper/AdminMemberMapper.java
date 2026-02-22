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

        // 2. 가입 기간(일수) 계산
        long joinDurationDays = 0;
        if (rawData.joinDate() != null) {
            joinDurationDays = ChronoUnit.DAYS.between(rawData.joinDate(), today);
        }

        // 3. 주소 문자열 조합
        String fullAddress = "";
        if (rawData.province() != null && rawData.city() != null && rawData.streetAddress() != null) {
            fullAddress = String.format("%s %s %s", rawData.province(), rawData.city(), rawData.streetAddress());
        }

        // 4. 이름과 전화번호 복호화
        String decryptedName = rawData.encryptedName() != null ? decryptionTool.decrypt(rawData.encryptedName()) : null;
        String decryptedPhone = rawData.encryptedPhone() != null ? decryptionTool.decrypt(rawData.encryptedPhone()) : null;

        // 5. 최종 응답 DTO(record) 생성 후 반환
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
                joinDurationDays,
                rawData.status()
        );
    }
}