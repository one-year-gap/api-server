package site.holliverse.auth.dto;

/**
 * 회원가입 성공 시 반환되는 데이터 DTO.
 */
public record SignUpDataResponseDto(
        /** 생성된 회원 ID. */
        Long memberId
) {
}