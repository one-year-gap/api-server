package site.holliverse.auth.dto;

/**
 * 회원가입 유스케이스 내부 반환 DTO.
 */
public record SingUpResponse(
        /** 생성된 회원의 기본키. */
        Long memberId
) {
}