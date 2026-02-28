package site.holliverse.admin.web.dto.member;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 관리자 회원 목록 조회 요청 DTO
 * - 검색어, 필터링 조건, 페이징 정보를 담는다.
 */
public record AdminMemberListRequestDto(
        @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
        Integer page,
        @Min(value = 1, message = "사이즈는 1 이상이어야 합니다.")
        Integer size,

        // 검색어 (이름 or 전화번호)
        String keyword,

        // 필터 조건들
        List<@Pattern(
                regexp = "^(VVIP|VIP|GOLD)$",
                message = "유효하지 않은 멤버십 등급입니다."
        ) String> memberships,
        @Pattern(regexp = "^(M|F)$", message = "성별은 M 또는 F만 가능합니다.")
        String gender,

        List<String> planNames,        // 구독 중인 상품 다중 선택

        // 연령대 필터링
        List<@Pattern(
                regexp = "^(UNDER_10|TEENS|TWENTIES|THIRTIES|FORTIES|FIFTIES|SIXTIES_EARLY|OVER_65)$",
                message = "유효하지 않은 연령대 필터값입니다."
        ) String> ages,

        // 가입 기간 필터링
        List<@Pattern(
                regexp = "^(UNDER_3_MONTHS|MONTHS_3_TO_12|YEARS_1_TO_2|YEARS_2_TO_5|YEARS_5_TO_10|OVER_10_YEARS)$",
                message = "유효하지 않은 가입기간 필터값입니다."
        ) String> durations,

        // 회원 상태 필터링
        List<@Pattern(
                regexp = "^(ACTIVE|BANNED|DELETED|PROCESSING)$",
                message = "유효하지 않은 회원 상태값입니다."
        ) String> statuses
) {
    // 생성자에서 null 처리 및 기본값 설정
    public AdminMemberListRequestDto {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        if (keyword != null) {
            keyword = keyword.replace("-", "");
        }
    }

    // offset 계산용 편의 메서드
    public int getOffset() {
        return (page - 1) * size;
    }
}