package site.holliverse.admin.web.dto.member;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminMemberUpdateRequestDto(

        // 1. 이름 검증: 길이 제한 및 특수문자 방지
        @Size(max = 20, message = "이름은 최대 20자까지만 입력 가능합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글 또는 영문만 입력 가능합니다.")
        String name,

        @Pattern(regexp = "^[0-9]+$", message = "전화번호는 숫자만 입력 가능합니다.")
        String phone,

        // 3. 상태값 검증: 정해진 상태만 통과 허용
        @Pattern(regexp = "^(ACTIVE|BANNED|DELETED|PROCESSING)$", message = "유효하지 않은 회원 상태값입니다.")
        String status,

        // 4. 멤버십 검증: 정해진 등급만 통과 허용
        @Pattern(regexp = "^(BASIC|GOLD|VIP|VVIP)$", message = "유효하지 않은 멤버십 등급입니다.")
        String membership
) {
}