package site.holliverse.customer.web.dto.counsel;

import jakarta.validation.constraints.NotBlank;

public record CreateCounselDto(
        @NotBlank(message = "상담 제목은 필수 값입니다.")
        String title,
        @NotBlank(message = "상담 내용은 필수 값입니다.")
        String content
) {
}
