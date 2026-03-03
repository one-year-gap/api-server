package site.holliverse.auth.dto;

/**==========================
 * 구글 회원가입 처음 시도할 시 추가정보 입력할때 이메일과 이름은 전송하기 위한 DTO
 * @author nonstop
 * @version 1.0.0
 * @since 2026-02-26
 * ========================== */
public record OnboardingPrefillResponseDto(
        String email,
        String name
) {
}
