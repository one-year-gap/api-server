package site.holliverse.admin.web.assembler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.holliverse.admin.query.dao.MemberRawData;
import site.holliverse.admin.web.dto.member.AdminMemberDto;
import site.holliverse.admin.web.dto.member.AdminMemberListResponseDto;
import site.holliverse.shared.util.DecryptionTool;

import java.util.List;
import java.util.stream.Collectors;

@Profile("admin")
@Component
@RequiredArgsConstructor
public class AdminMemberAssembler {

    private final DecryptionTool decryptionTool;

    /**
     * MemberRawData 리스트를 최종 응답 DTO로 변환 (복호화 + 마스킹 + 페이징 조립)
     */
    public AdminMemberListResponseDto toListResponse(List<MemberRawData> rawDataList, int totalCount, int page, int size) {
        List<AdminMemberDto> memberDtos = rawDataList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        // DTO의 of 메서드를 호출하여 최종 조립
        return AdminMemberListResponseDto.of(memberDtos, totalCount, page, size);
    }

    /**
     * 회원 한 명의 데이터를 복호화 및 마스킹하여 DTO로 변환
     */
    private AdminMemberDto toDto(MemberRawData raw) {
        // 1. 복호화 수행
        String decryptedName = decryptionTool.decrypt(raw.getEncryptedName());
        String decryptedPhone = decryptionTool.decrypt(raw.getEncryptedPhone());

        return AdminMemberDto.builder()
                .id(raw.getId())
                .membership(raw.getMembership())
                .gender(raw.getGender())
                .name(maskName(decryptedName))   // 마스킹 적용
                .phone(maskPhone(decryptedPhone)) // 마스킹 적용
                .birthDate(raw.getBirthDate())
                .email(raw.getEmail())
                .planName(raw.getPlanName())
                .build();
    }

    /**
     * 이름 마스킹: 김영현 -> 김*현, 남궁민수 -> 남**수
     */
    private String maskName(String name) {
        if (name == null || name.length() < 2) return name;
        if (name.length() == 2) return name.charAt(0) + "*";

        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    /**
     * 전화번호 마스킹 및 포맷팅: 01012345678 -> 010-****-5678
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) return phone;

        // 하이픈이 없는 숫자로만 온다고 가정할 때 (01012345678)
        String cleanPhone = phone.replaceAll("-", "");

        return cleanPhone.substring(0, 3) + "-****-" + cleanPhone.substring(cleanPhone.length() - 4);
    }
}