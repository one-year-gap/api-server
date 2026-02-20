package site.holliverse.admin.web.dto.member;

import jakarta.validation.constraints.Pattern;

public record AdminMemberUpdateRequestDto(

        String name,

        @Pattern(regexp = "^[0-9]+$", message = "전화번호는 숫자만 입력 가능합니다.")
        String phone,

        String status,

        String membership
) {
}